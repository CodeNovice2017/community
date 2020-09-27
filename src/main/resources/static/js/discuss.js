function like(btn,entityType,entityId){
    $.post(
        CONTEXT_PATH + "/like",
        {
            "entityType":entityType,
            "entityId":entityId
        },
        function(data){
            data = $.parseJSON(data);
            if(data.code == 0){
                // 我们在页面传入了this对应的按钮,那么就可以得到<a>标签下设置的<b><i>标签
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else{
                alert(data.msg);
            }
        }
    );
}