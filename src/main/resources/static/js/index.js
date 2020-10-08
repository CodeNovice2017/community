$(function(){
	// 发布按钮触发的逻辑
	$("#publishBtn").click(publish);
});

function publish() {
	// 点击发布按钮后,发布界面隐藏
	$("#publishModal").modal("hide");

	// 我们已经启用了CSRF攻击,那么我们每一个异步请求都要这么处理,如果不处理其他的异步请求也像上面更改,
	// 服务器无法得到token,那么这个时候就会认为被攻击了,就不让访问了,本项目为了省事就不添加CSRF了

	// 发送AJAX请求之前,将CSRF令牌设置到请求消息头中
	// 获取meta中name=_csrf的meta元素
	// var token = $("meta[name='_csrf']").attr("content");
	// var header = $("meta[name='_csrf_header']").attr("content");
	// 发送请求之前对请求参数做一个设置
	// 传入一个匿名函数,其中xhr就是发送异步请求的核心对象,需要通过他设置请求头
	// 这样设置以后,请求里面就会携带header和token数据
	// $(document).ajaxSend(function(e,xhr,options){
	// 	xhr.setRequestHeader(header,token);
	// });

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