$(function(){
	// 发布按钮触发的逻辑
	$("#publishBtn").click(publish);
});

function publish() {
	// 点击发布按钮后,发布界面隐藏
	$("#publishModal").modal("hide");

	// 获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	// 发送异步请求
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title,"content":content},
		// 回调函数,在这里写逻辑能保证是在接收到服务器返回的响应JSON数据后调用,并且服务器的响应JSON已经存在了data中
		function(data){
			data = $.parseJSON(data);
			// 在提示框当中显示返回的消息
			$("#hintBody").text(data.msg);
			// 在提示框中返回显示消息以后,要显示提示框
			// 显示后,两秒后自动隐藏提示框
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 如果说我们返回的结果OK,表示数据在服务端已经写入数据库了,那我就要把当前的页面刷新一下
				// 如果说添加失败了,就不刷新页面了,但是无论成功失败都给了提示
				if(data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	)

}