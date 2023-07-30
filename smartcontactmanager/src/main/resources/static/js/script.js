console.log("this is script file")


const toggleSidebar = () => {
	if ($('.sidebar').is(":visible")) {

		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");

	} else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}

};

const search = () => {
	//console.log("searching....");


	let query = $("#search-input").val();
	if (query === '') {
		$(".search-result").hide();
	} else {


		//sending request from server

		let url = `http://localhost:8080/search/${query}`;

		fetch(url).then((response) => {
			return response.json();

		}).then((data) => {
			//data



			let text = `<div class='list-group'>`;

			data.forEach((contact) => {
				text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-action'>${contact.name}</a>`;
			});


			text += `</div>`;


			$(".search-result").html(text);
			$(".search-result").show();

		});



	}

};



//first request -  to server to create order


const paymentStart = () => {




	let amount = $("#payment_field").val();
	if (amount == '' || amount == null) {
		//alert("Please enter the Amount");
		swal("Failed !!", " Amount is required!!", "error");
		return;
	}

	//code
	//we will use ajax to send request to server to create order - jquery

	$.ajax(
		{
			url: "/user/create_order",
			data: JSON.stringify({ amount: amount, info: "order_request" }),
			contentType: 'application/json',
			type: "POST",
			dataType: "json",
			success: function (response) {
				//invoked when success
				console.log(response);
				if (response.status === "created") {
					//open payment form
					let options = {
						key: 'rzp_test_g7uMVHBCxNGxcK',
						amount: response.amount,
						currency: 'INR',
						name: 'Smart Contact Manager', //optional
						description: 'Donation',
						image: "https://media.licdn.com/dms/image/D4D03AQE6T8N5ETFsVw/profile-displayphoto-shrink_800_800/0/1678601480472?e=2147483647&v=beta&t=cTmOLkBteaOK4WQLQO2P34LolFeVl0q9cZmM7HuB1V4",
						order_id: response.id,
						handler: function (response) {
							console.log(response.razorpay_payment_id);
							console.log(response.razorpay_order_id);
							console.log(response.razorpay_signature);
							console.log('payment successful !!');
							//alert("cognates !! payment successful !!");
							swal("Good job!", "cognates !! payment successful !!", "success");
						},
						prefill: {
							name: "",
							email: "",
							contact: "",
						},
						notes: {
							address: "Kaushal Sachin Divekar",

						},
						theme: {
							color: "#3399cc",
						},

					};
					let rzp = new Razorpay(options);
					rzp.on("payment.failed", function (response) {
						console.log(response.error.code);
						console.log(response.error.description);
						console.log(response.error.source);
						console.log(response.error.step);
						console.log(response.error.reason);
						console.log(response.error.metadata.order_id);
						console.log(response.error.metadata.payment_id);
						//alert("Oops Payment failed !!");
						swal("Failed !!", "Payment failed !!", "error");
					});

					rzp.open();


				}
			},
			error: function (error) {
				//invoke when error
				console.log(error);
				alert("something went wrong.....")
			}

		}

	)

};

