package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    // 统计数据的话,就是两个方面
    // 第一个方面是我要把数据记录下来,在每次请求当中我要截获这个请求,把相关数据记录到Redis中
    // 第二个当我想看的时候,我能通过查询访问到
    // UV统计
    // 将指定的IP计入UV
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    // 统计指定日期范围内的UV
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 如果参数没问题就处理
        // 统计范围内的UV的话,就是将范围内每一天的Key做一个合并
        // 我们先得得到这日期范围内的一组key,先搜集到这一组key,做成一个数组才行
        // 然后调用合并的方法

        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        // 日期的遍历方法,对日期做运算需要用到Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        // calendar.getTime()获取日期时间
        // calendar.getTime().after(end) 这个时间如果晚于end这个Date
        // !calendar.getTime().after(end) 时间小于end的时候就循环
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            // 指定每次加一天,Calendar.DATE指定为天
            calendar.add(Calendar.DATE, 1);
        }

        // 合并以后数据的Key
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        // 合并这些数据,第二个参数是待合并的Key的数组
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    // 将指定用户计入DAU
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        // 以userId作为索引
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    // 统计指定日期范围内的DAU
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }


        // 整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            // 不同点:Bitmap运算方法需要的Key的类型是Byte数组,所以集合里放的是Key的Byte数组形式
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        // DAU不同于UV,UV用的是HyperLogLog,指定日期范围的统计,还是要要去重(Hash这一步就去重了,因为用户登录的ip相同,所以Hash计算之后是相同的)
        // 那么同一个IP计算出的Hash一直都是相同的,自然落在对应索引的桶中也只有一个
        // 进行OR运算,就是范围内任何一天这个userId活跃了,那都算活跃了
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        // keyList.toArray(new byte[0][0])转成了一个二维的Byte数组
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
