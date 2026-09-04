package com.csquare.lc.ms.orders.kafka;

import com.csquare.lc.ms.orders.lib.model.*;
import com.csquare.ms.lib.utils.SystemHelper;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

//@SpringBootTest
class MsPurchaseOrderApplicationTests {

	/*SystemHelper helper = new SystemHelper();

	@Test
	public void testTime() {
		System.out.println(helper.getCurrentTime().toString());
		TreeSet<String> tset = new TreeSet<>();

		// Adding elements to TreeSet<String>
		tset.add("5");
		tset.add("1");
		tset.add("Test");
		tset.add("Pen");
		tset.add("Ink");
		tset.add("Jack");

		//Displaying TreeSet
		System.out.println(tset);
	}

	@Test
	public void testOrderJson() {
		Long userId = 1L;
		LocalDateTime time = helper.getCurrentTime();

		OrderMst mst = new OrderMst(userId, time);
		mst.setCC2code("00400");
		mst.setCBrCode("002");
		mst.setCCancelFlag("N");
		mst.setCCustCode("001003");
		mst.setCDeliveryPickupFlag("N");
		mst.setCInvoiceRefNo("");
		mst.setCNote("dummy entry");
		mst.setCOrderStatus("I");
		mst.setCSourceRefNo("");
		mst.setCRepFlag("N");
		mst.setCSmanCode("");
		mst.setCUserTypeFlag("no");
		mst.setNDeliveryCharge(BigDecimal. valueOf(10.00));
		mst.setNDiscountAmount(BigDecimal. valueOf(0.00));
		mst.setNItemTotal(BigDecimal. valueOf(105.50));
		mst.setNNetPayableAmount(BigDecimal. valueOf(95.50));

		List<OrderDet> orderDetList = new ArrayList<>();
		OrderDet orderDet = new OrderDet(userId, time);
		orderDet.setCBuyerItemcode("AAA111");
		orderDet.setCBuyerItemName("gemer");
		orderDet.setCCancelFlag("N");
		orderDet.setCItemCode("101600");
		orderDet.setCNote("");
		orderDet.setCRateConsiderFlag("no");
		orderDet.setCUserFlag("N");
		orderDet.setNActualRate(BigDecimal.valueOf(15.50));
		orderDet.setNMrp(BigDecimal.valueOf(17.50));
		orderDet.setNSaleRate(BigDecimal.valueOf(15.15));
		orderDet.setNQty(BigDecimal.valueOf(10));
		orderDet.setNItemTotal(BigDecimal.valueOf(151.50));
		orderDet.setNDiscPercentage(BigDecimal.valueOf(0));
		orderDet.setNSchemePer(BigDecimal.valueOf(0));
		orderDet.setNSchemeQty(BigDecimal.valueOf(0));
		orderDet.setNDiscAmount(BigDecimal.valueOf(10));
		orderDetList.add(orderDet);
		mst.setOrderDets(orderDetList);

		ContactDetail contactDetail = new ContactDetail(userId, time);
		contactDetail.setCAddress1("aaaaaaaa");
		contactDetail.setCAddress2("qqqqqqqq");
		contactDetail.setCAlternativeMobileNo("1111111111");
		contactDetail.setCAlternativeEmailId("dummy@gmail.com");
		contactDetail.setCAlternativePhoneNo("3333333333");
		contactDetail.setCCity("bengaluru");
		contactDetail.setCContactName("Ronald");
		contactDetail.setCCountry("INDIA");
		contactDetail.setCEmailId("cr7@gmail.com");
		contactDetail.setCMobileNo("2222222222");
		contactDetail.setCNote("contact details note");
		contactDetail.setCPhoneNo("9999999999");
		contactDetail.setCPin("560040");
		contactDetail.setCState("KA");

		DeliveryDetail deliveryDetail = new DeliveryDetail(userId, time);
		deliveryDetail.setCCode("001203");
		deliveryDetail.setCMode("xyz");
		deliveryDetail.setCName("Cris");
		deliveryDetail.setCNote("delivery note");
		deliveryDetail.setCStatus("I");
		deliveryDetail.setContactDetail(contactDetail);
		deliveryDetail.setTInitiate(null);
		deliveryDetail.setTLastUpdatedAt(null);
		mst.setDeliveryDetail(deliveryDetail);

		Prescription prescription = new Prescription();
		prescription.setCDoctorName("Kabir");
		prescription.setCPatientName("Naina");
		prescription.setContactDetail(contactDetail);
		LocalDate expDate = LocalDate.parse("2019-07-23");

		List<PrescriptionDoc> prescriptionDocList = new ArrayList<>();
		PrescriptionDoc prescriptionDoc = new PrescriptionDoc(userId, time);
		prescriptionDoc.setCUrl("https://www.google.com/imghp");
		prescriptionDocList.add(prescriptionDoc);
		prescription.setPrescriptionDocs(prescriptionDocList);

		mst.setPrescription(prescription);

		ShippingDetail shippingDetail = new ShippingDetail(userId, time);
		shippingDetail.setCCode("shan");
		shippingDetail.setCMode("qq");
		shippingDetail.setCCode("S001");
		shippingDetail.setCNote("shipping Note");
		shippingDetail.setContactDetail(contactDetail);
		shippingDetail.setCStatus("I");
		shippingDetail.setTCompleted(null);
		mst.setShippingDetail(shippingDetail);

		List<PaymentDetail> paymentDetailList = new ArrayList<>();
		PaymentDetail paymentDetail = new PaymentDetail(userId, time);
		paymentDetail.setCPaymentRefCode("dbh74hd09jf6hw537dm85dhd5ku64n");
		paymentDetail.setCPaymentStatus("pending");
		paymentDetail.setCPaymentType("UPI");
		paymentDetail.setCTransactionType("");
		paymentDetailList.add(paymentDetail);
		mst.setPaymentDetails(paymentDetailList);

		JsonObject object = (JsonObject) helper.getGson().toJsonTree(mst,
				new TypeToken<OrderMst>() {
				}.getType());
		System.out.println(object);
	}

	@Test
	public void saveUserTest() {
		Long userId = 1L;
		LocalDateTime time = helper.getCurrentTime();

		OrderMst mst = new OrderMst(userId, time);
		mst.setCC2code("00400");
		mst.setCBrCode("001");
		mst.setCCancelFlag("no");
		mst.setCCustCode("001003");
		mst.setCDeliveryPickupFlag("no");
		mst.setCInvoiceRefNo("");
		mst.setCNote("dummy entry");
		mst.setCOrderStatus("I");
		mst.setCSourceRefNo("");
		mst.setCRepFlag("no");
		mst.setCSmanCode("");
		mst.setCUserTypeFlag("no");
		mst.setNDeliveryCharge(BigDecimal. valueOf(10.00));
		mst.setNDiscountAmount(BigDecimal. valueOf(0.00));
		mst.setNItemTotal(BigDecimal. valueOf(105.50));
		mst.setNNetPayableAmount(BigDecimal. valueOf(95.50));

		List<OrderDet> orderDetList = new ArrayList<>();
		OrderDet orderDet = new OrderDet(userId, time);
		orderDet.setCBuyerItemcode("AAA111");
		orderDet.setCBuyerItemName("gemer");
		orderDet.setCCancelFlag("no");
		orderDet.setCItemCode("101600");
		orderDet.setCNote("");
		orderDet.setCRateConsiderFlag("no");
		orderDet.setCUserFlag("no");
		orderDet.setNActualRate(BigDecimal.valueOf(15.50));
		orderDet.setNMrp(BigDecimal.valueOf(17.50));
		orderDet.setNSaleRate(BigDecimal.valueOf(15.15));
		orderDet.setNQty(BigDecimal.valueOf(10));
		orderDet.setNItemTotal(BigDecimal.valueOf(151.50));
		orderDet.setNDiscPercentage(BigDecimal.valueOf(0));
		orderDet.setNSchemePer(BigDecimal.valueOf(0));
		orderDet.setNSchemeQty(BigDecimal.valueOf(0));
		orderDet.setNDiscAmount(BigDecimal.valueOf(10));
		orderDetList.add(orderDet);
		mst.setOrderDets(orderDetList);

		ContactDetail contactDetail = new ContactDetail(userId, time);
		contactDetail.setCAddress1("aaaaaaaa");
		contactDetail.setCAddress2("qqqqqqqq");
		contactDetail.setCAlternativeMobileNo("1111111111");
		contactDetail.setCAlternativeEmailId("dummy@gmail.com");
		contactDetail.setCAlternativePhoneNo("3333333333");
		contactDetail.setCCity("bengaluru");
		contactDetail.setCContactName("Ronald");
		contactDetail.setCCountry("INDIA");
		contactDetail.setCEmailId("cr7@gmail.com");
		contactDetail.setCMobileNo("2222222222");
		contactDetail.setCNote("contact details note");
		contactDetail.setCPhoneNo("9999999999");
		contactDetail.setCPin("560040");
		contactDetail.setCState("KA");

		DeliveryDetail deliveryDetail = new DeliveryDetail(userId, time);
		deliveryDetail.setCCode("001203");
		deliveryDetail.setCMode("xyz");
		deliveryDetail.setCName("Cris");
		deliveryDetail.setCNote("delivery note");
		deliveryDetail.setCStatus("I");
		deliveryDetail.setContactDetail(contactDetail);
		deliveryDetail.setTInitiate(null);
		deliveryDetail.setTLastUpdatedAt(null);
		mst.setDeliveryDetail(deliveryDetail);

		Prescription prescription = new Prescription();
		prescription.setCDoctorName("Kabir");
		prescription.setCPatientName("Naina");
		prescription.setContactDetail(contactDetail);
		LocalDate expDate = LocalDate.parse("2019-07-23");

		List<PrescriptionDoc> prescriptionDocList = new ArrayList<>();
		PrescriptionDoc prescriptionDoc = new PrescriptionDoc(userId, time);
		prescriptionDoc.setCUrl("https://www.google.com/imghp");
		prescriptionDocList.add(prescriptionDoc);
		prescription.setPrescriptionDocs(prescriptionDocList);

		mst.setPrescription(prescription);

		ShippingDetail shippingDetail = new ShippingDetail(userId, time);
		shippingDetail.setCCode("shan");
		shippingDetail.setCMode("qq");
		shippingDetail.setCCode("S001");
		shippingDetail.setCNote("shipping Note");
		shippingDetail.setContactDetail(contactDetail);
		shippingDetail.setCStatus("I");
		shippingDetail.setTCompleted(null);
		mst.setShippingDetail(shippingDetail);

		List<PaymentDetail> paymentDetailList = new ArrayList<>();
		PaymentDetail paymentDetail = new PaymentDetail(userId, time);
		paymentDetail.setCPaymentRefCode("dbh74hd09jf6hw537dm85dhd5ku64n");
		paymentDetail.setCPaymentStatus("pending");
		paymentDetail.setCPaymentType("UPI");
		paymentDetail.setCTransactionType("");
		paymentDetailList.add(paymentDetail);
		mst.setPaymentDetails(paymentDetailList);

		JsonObject object = (JsonObject) helper.getGson().toJsonTree(mst,
				new TypeToken<OrderMst>() {
				}.getType());
		System.out.println(object);

	}

	@Test
	void toJsonCartMst () {
		CartMst mst = new CartMst();
		mst.setIdTime(1L, helper.getCurrentTime());
		mst.setCMobileNo("1111111111");
		mst.setCNote("Manufacturer name");
		mst.setCBuyerCode("004000");
		mst.setNTotal(BigDecimal.valueOf(10000.0));

		List<CartDet> dets = new ArrayList<>();
		CartDet cartDet = new CartDet();
		cartDet.setIdTime(1L, helper.getCurrentTime());
		cartDet.setNSeq(1L);
		cartDet.setNSchemeQty(BigDecimal.valueOf(0.0));
		cartDet.setNQty(BigDecimal.valueOf(100));
		cartDet.setNMrp(BigDecimal.valueOf(67));
		cartDet.setNPtr(BigDecimal.valueOf(60));
		cartDet.setNSchemePer(BigDecimal.valueOf(0));
		cartDet.setNGstPerc(BigDecimal.valueOf(100));
		cartDet.setNSaleRate(BigDecimal.valueOf(65));
		cartDet.setNDiscountPer(BigDecimal.valueOf(5));
		dets.add(cartDet);
		mst.setCartDets(dets);

		JsonObject object = (JsonObject) helper.getGson().toJsonTree(mst,
				new TypeToken<CartMst>() {
				}.getType());
		System.out.println(object);
	}

	@Test
	void toLcOrderBO ()  {

		LcOrder obj = new LcOrder();
		obj.setOrderId("1");
		System.out.println(helper.toJson(obj));
	}*/
/*
	@Test
	@DisplayName("1 + 1 = 2")
	void addsTwoNumbers() {
		Calculator calculator = new Calculator();
		assertEquals(2, calculator.add(1, 1), "1 + 1 should equal 2");
	}

	@ParameterizedTest(name = "{0} + {1} = {2}")
	@CsvSource({
			"0,    1,   1",
			"1,    2,   3",
			"49,  51, 100",
			"1,  100, 101"
	})
	void add(int first, int second, int expectedResult) {
		Calculator calculator = new Calculator();
		assertEquals(expectedResult, calculator.add(first, second),
				() -> first + " + " + second + " should equal " + expectedResult);
	}*/

}