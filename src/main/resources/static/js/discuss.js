//等页面加载完成后的事件绑定
$(function () {
    $("#setTopBtn").click(setTop);
    $("#setWonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);


})


//置顶
function setTop() {

    $.post(

        CONTEXT_PATH+"/discuss/top",
        {"id":$("#postId").val()},

        function (data) {
            data=$.parseJSON(data);
            if(data.code==0){
                $("#setTopBtn").attr("disabled","disabled");//置顶成功后就不能再点了
            }else{
                alert(data.msg)
            }
        }

    );
}



//加精
function setWonderful() {
        $.post(
            CONTEXT_PATH+"/discuss/wonderful",
            {"id":$("#postId").val()},

            function (data) {
                data=$.parseJSON(data);
                if(data.code==0){
                    $("#setWonderfulBtn").attr("disabled","disabled");
                }else {
                    alert(data.msg);
                }
            }

        );
}

//删除
function setDelete() {
    $.post(
        CONTEXT_PATH+"/discuss/delete",
        {"id":$("#postId").val()},

        function (data) {
            data=$.parseJSON(data);
            if(data.code==0){
                //删除成功就直接跳转到首页
                location.href=CONTEXT_PATH+"/index";

            }else {
                alert(data.msg);
            }
        }

    );
}

function like(btn, entityType, entityId,entityUserId,postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':"赞");
            } else {
                alert(data.msg);
            }
        }
    );
}