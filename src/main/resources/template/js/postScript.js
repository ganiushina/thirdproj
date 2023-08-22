
$(document).ready(function () {
    $("#checked-contacts").submit(function (event) {
        var productData = {}
        productData["productName"] = $("#productNameId").val();
        productData["productQuantity"] = $("#productQuantityId").val();

        $("#success-msg").hide();
        $("#error-msg").hide();

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: "/payment/confirm",
            data: JSON.stringify(productData),
            dataType: 'json',
            cache: false,
            timeout: 60000,
            success: function (data) {
                $("#success-msg").show();
            },
            error: function (e) {
                $("#error-msg").show();
            }
        });
    });
});