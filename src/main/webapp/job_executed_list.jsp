<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%
    String path = request.getContextPath();
			String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ path + "/";
%>
<div class="pageHeader">
	<div class="searchBar">
		<ul class="searchContent">
			<li>已执行的任务列表，每20秒刷新一次</li>
		</ul>
	</div>
</div>

<div class="pageContent">
	<div class="panelBar">
		<ul class="toolBar">
			<li><a class="icon"
				href="javascript:$.printBox('w_list_print2')"><span>打印</span></a></li>
		</ul>
	</div>
	<div id="w_list_print2" style="height: 700px"
		onblur="alert('hehhehe');"></div>
</div>

<script type="text/javascript" language="javascript">
	startRequest();
	var intervalId;
	$(document).ready(function() {
		intervalId = setInterval("startRequest()", 20000);

	});

	function startRequest() {
		$.ajax({
			url : "job/jsonList",
			type : 'GET',
			success : callback
		});
	}
	function callback(json) {
		var obj = eval("(" + json + ")");
		var new_table_head = '<table class="list" width="98%" targetType="navTab" asc="asc"'
				+ '	desc="desc" layoutH="116" id="listTable">'
				+ '	<thead>'
				+ '		<tr>'
				+ '			<th width="80">Job名称</th>'
				+ '			<th width="60">所属Scheduler</th>'
				+ '			<th width="60">预定义的触发时间</th>'
				+ '			<th width="60">实际触发时间</th>'
				+ '			<th width="60">下一次触发时间</th>'
				+ '			<th width="60">上一次出发时间</th>'
				+ '			<th width="10">Duration</th>' + '		</tr>' + '	</thead>'
		if (obj['data'].length == 0) {
			$("#w_list_print2 table").remove();
			var new_table = new_table_head
					+ '<tbody><tr><td colspan="7"><font color="red">'
					+ '没有数据</font></td></tr></tbody></table>';
			$("#w_list_print2").append(new_table);
		} else {
			$("#w_list_print2 table").remove();
			var new_table = new_table_head + '<tbody></tbody></table>';
			$("#w_list_print2").append(new_table);
			var tb = $("#listTable");
			var str;
			for (var i = 0; i < obj['data'].length; i++) {
				str = "<tr><td>" + obj['data'][i]['jobName'] + '</td>' + '<td>'
						+ obj['data'][i]['schedulerName'] + '</td>' + '<td>'
						+ obj['data'][i]['scheduledFireTime'] + '</td>'
						+ '<td>' + obj['data'][i]['fireTime'] + '</td>'
						+ '<td>' + obj['data'][i]['nextFireTime'] + '</td>'
						+ '<td>' + obj['data'][i]['previousFireTime'] + '</td>'
						+ '<td>' + obj['data'][i]['jobRunTime'] + 'ms</td>';
				tb.append(str);
			}
		}
	}
	function clearMyInterval() {
		window.clearInterval(intervalId);
	}
	$("#w_list_print2").blur(function() {
		alert("heihei");
		clearMyInterval();
	});
	function hehe() {
		alert('hehe');
	}
	$("#pageContent").blur(hehe);
	$("#pageContent").change(hehe);
</script>
