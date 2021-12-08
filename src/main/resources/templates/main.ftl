<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Document</title>
    <#import "common.ftl" as commonFtl>
</head>
<body>

<h1>dhsajdhkjks</h1>
<span>name ===</span><span> ${user.name} </span>
<span>desc ===</span><span> ${user.desc} </span>
<textarea id="input"></textarea>
<div id="result">xxxxxxxxxxxxxxxxxxxxxxxxxxx</div>
<button id="submit">提交</button>
</body>
<@commonFtl.commonScript />
<script src="/deskspaceserver/js/main.js"></script>
</html>
