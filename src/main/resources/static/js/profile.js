$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			CONTEXT_PATH + "/follow",
			{
				entityType:3,
				entityId:$(btn).prev().val()
			},
			function(data){
				data = $.parseJSON(data);
				if(data.code == 0){
					// 因为如果我们想实现异步请求的,应该利用像下面的JS代码
					// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
					// 对样式进行更改,然后同时从data获取返回的数据,然后在对关注的值进行修改
					// 在这个项目前端不是主要的,所以直接偷懒刷新一下页面
					window.location.reload();
				}else{
					alert(data.msg);
				}
			}
		);

	} else {
		// 取消关注
		$.post(
			CONTEXT_PATH + "/unfollow",
			{
				entityType:3,
				entityId:$(btn).prev().val()
			},
			function(data){
				data = $.parseJSON(data);
				if(data.code == 0){
					window.location.reload();
				}else{
					alert(data.msg);
				}
			}
		);
	}
}