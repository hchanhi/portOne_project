function requestPay() {

        var IMP = window.IMP;
        IMP.init("imp37173674");
        const orderNumber = document.getElementById('orderNumber').value;
        const productName = document.getElementById('productName').value;
        const paymentAmount = document.getElementById('paymentAmount').value;

        IMP.request_pay(
            {
                pg: "nice.nictest00m",
                pay_method: "card",
                merchant_uid: orderNumber,
                name: productName,
                amount: paymentAmount,
                buyer_email: "Iamport@chai.finance",
                buyer_name: "포트원 기술지원팀",
                buyer_tel: "010-1234-5678",
                buyer_addr: "서울특별시 강남구 삼성동",
                buyer_postcode: "123-456",
            },
            function (rsp) {
                if (rsp.success) {
                    // 결제 성공 시: 결제 승인 또는 가상계좌 발급에 성공한 경우
                    alert("결제가 성공적으로 처리되었습니다.");

                    // jQuery로 HTTP 요청
                    // jQuery.ajax({
                    //     url: "{서버의 결제 정보를 받는 가맹점 endpoint}",
                    //     method: "POST",
                    //     headers: { "Content-Type": "application/json" },
                    //     data: {
                    //         imp_uid: rsp.imp_uid,            // 결제 고유번호
                    //         merchant_uid: rsp.merchant_uid   // 주문번호
                    //     }
                    // }).done(function (data) {
                    //     // 가맹점 서버 결제 API 성공시 로직
                    //     alert("결제가 성공적으로 처리되었습니다.");
                    // })
                } else {
                    alert("결제에 실패하였습니다. 에러 내용: " + rsp.error_msg +
                    "거래 번호: " + orderNumber);
                }
            });
    }