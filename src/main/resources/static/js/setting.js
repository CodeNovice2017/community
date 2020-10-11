// 给setting表单的form,定义一个事件,当我们点击提交按钮的时候,其实是触发表单的提交事件,我需要定义表单的提交事件
// $(function(){})表示页面加载以后,我要调用这个function函数
$(function(){
    // 给form重写提交事件
    // 下面的代码表示:当我点击提交按钮,触发表单的提交事件时,这个事件由upload函数来处理
    $("#uploadForm").submit(upload);
});

function upload(){

    // 因为表单的数据比较特别,是一个图片文件,所以就不能用$.post简单的来定义了,因为$.post是一个简化的AJAX请求方式,有些参数无法设置
    // 所以使用$.ajax({}),未简化的ajax请求,{}内可以包含多组key,value,每一组key-value是一个参数,所有的参数都可以定义
    $.ajax({
        // 请求提交给谁
        // 七牛云的上传路径:可以在文档手册->存储区域->华北z1->客户端上传的路径
        url: "http://upload-z1.qiniup.com",
        method : "post",
        // 默认情况下提交表单,浏览器会把表单的内容转化为字符串提交给服务器,但是现在是上传文件,不能转为字符串
        processData : false,
        // 一般来说contentType应该写html,json等,写的是传的数据类型,写为false代表不让jquery设置上传的类型,那么浏览器会自动设置
        // 因为浏览器提交文件的时候,文件和别的数据不同,文件是2进制的,它和别的数据混在一起的时候,这个边界怎么确定,浏览器会给加一个随机的边界字符串
        // 好去拆分这个内容,如果这里我们指定了contentType,那么jquery就会自动的设置数据类型,就会导致数据边界设置不上,就会导致上传的文件出现问题
        // 所以只能设置为false
        contentType: false,
        // 要传的数据是什么,需要特殊处理,因为是文件类型,所以需要FormData这个js对象,要把form传入进来$("#uploadForm")
        // 但是$("#uploadForm")是jquery对象,而这里需要的是原生的js对象,所以需要$("#uploadForm")[0],因为jquery对象本质上就是dom对象的数组
        // 当从数组取某一个值的时候就能取到dom
        data : new FormData($("#uploadForm")[0]),

        // 成功的时候的处理
        success : function (data){
            // 如果data存在并且data.code==0
            if(data && data.code==0){
                // 上传七牛云成功的话,需要更新头像的访问路径,需要在页面上用异步的方式访问Controller,更新头像
                // 还是异步的请求
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {
                        // fileName可以直接从页面获得,$("input")元素选择器选择所有的input元素,然后属性选择器[name='key']
                        "fileName":$("input[name='key']").val()
                    },
                    function (data){
                        // 上层if可以直接用data当做对象是因为,上面的data是七牛云返回的,原本就是JSON格式的数据
                        // 我们这里返回的是普通的格式为JSON的字符串,但是并没有声明返回的是JSON,所以需要转换一下
                        data = $.parseJSON(data);
                        if(data.code == 0){
                            window.location.reload();
                        }else{
                            alert(data.msg);
                        }
                    }
                );
            }else{
                alert("上传失败!")
            }
        }
    });

    // 最终一定要return false; 否则的话虽然执行了前面的逻辑,但是最后还是会提交表单,而form已经没有了action,
    // return false就是代表不要再提交了,我们上面的请求已经把这个事件处理完了,表示事件到此为止,不要再向底层执行原有的事件了
    return false;
}