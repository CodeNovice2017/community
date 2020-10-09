// 通常要写一个逻辑,就是在页面加载完以后,说明html标签都加载完了,这个时候我用JS给这个标签动态绑定一个事件,一般是这样做
// $(function(){});这句话的意思和window.onload=function(){}是一样的,表示页面加载事件的意思,这个函数会在页面加载完调用
// 给这三个方法绑定事件
$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

function like(btn,entityType,entityId,entityUserId,postId){
    $.post(
        CONTEXT_PATH + "/like",
        {
            "entityType":entityType,
            "entityId":entityId,
            "entityUserId":entityUserId,
            "postId":postId
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

// 置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                // 成功的话,我们要修改按钮的可用性,如果我点过按钮了,那么就不用再点第二次了
                // 通过设置标签属性 disabled="disabled"
                // 就是成功的时候需要把按钮设置为不可用
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                // 如果帖子删除完以后,我就不用修改按钮可用性了,我也不会再看这个帖子了,直接跳转到首页
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}