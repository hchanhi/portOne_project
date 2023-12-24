function requestPay() {

        var IMP = window.IMP;
        IMP.init("imp37173674");
        const merchant_uid = document.getElementById('merchant_uid').value;
        const productName = document.getElementById('productName').value;
        const paymentAmount = document.getElementById('paymentAmount').value;

        IMP.request_pay(
            {
                pg: "nice.nictest00m",
                pay_method: "card",
                merchant_uid: merchant_uid,
                name: productName,
                amount: paymentAmount,
                buyer_email: "hcahni@aaa.com",
                buyer_name: "한**",
                buyer_tel: "010-1234-5678",
                buyer_addr: "서울특별시 ***구 **동",
                buyer_postcode: "123-456",
            },
            function (rsp) {
                if (rsp.success) {
                    // 결제 성공 시: 결제 승인 또는 가상계좌 발급에 성공한 경우
                    console.log(rsp.imp_uid);
                    alert("결제가 성공적으로 처리되었습니다.\n 거래 번호 : " + rsp.imp_uid);

                } else {
                    alert("결제에 실패하였습니다. 에러 내용: " + rsp.error_msg +
                    "거래 번호: " + merchant_uid);
                }
            });
    }