package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

public class FareCalculatorServiceTest {

	private static FareCalculatorService fareCalculatorService;
	private Ticket ticket;
	private double ticketPrice;

	@BeforeAll
	private static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	private void setUpPerTest() {
		ticket = new Ticket();
	}

	@Test
	public void calculateFareCar() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		
		ticketPrice = ticket.getPrice().doubleValue(); //convert BigDecimal to Double to match CAR_RATE_PER_HOUR
		assertEquals(ticketPrice, Fare.CAR_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareBike() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		
		ticketPrice = ticket.getPrice().doubleValue(); //convert BigDecimal to Double to match BIKE_RATE_PER_HOUR
		assertEquals(Fare.BIKE_RATE_PER_HOUR, ticketPrice);
	}

	@Test
	public void calculateFareUnkownType() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void calculateFareBikeWithFutureInTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void calculateFareBikeWithLessThanOneHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th
																		// parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		
		ticketPrice = ticket.getPrice().doubleValue();
		assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticketPrice);
	}

	@Test
	public void calculateFareCarWithLessThanOneHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th
																		
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		BigDecimal ticketPrice = new BigDecimal(ticket.getPrice().doubleValue());
		
		BigDecimal expectedPrice = new BigDecimal(1).multiply(new BigDecimal(0.75)).multiply(new BigDecimal(Fare.CAR_RATE_PER_HOUR));
		BigDecimal expectedTicketPrice = expectedPrice.setScale(2, RoundingMode.DOWN);
		
		assertEquals(expectedTicketPrice, ticketPrice.setScale(2, RoundingMode.DOWN));
		
	}

	@Test
	public void calculateFareCarWithMoreThanADayParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));// 24 hours parking time should give 24 *
																			// parking fare per hour
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		
		ticketPrice = ticket.getPrice().doubleValue(); //convert BigDecimal to Double to match CAR_RATE_PER_HOUR
		assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticketPrice);
	}

	@Test
	public void calculateFareCarWithLessThan30minutesParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (30 * 60 * 1000)); // 30 minutes parking time should give 0€ parking
																		// fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		
		ticketPrice = ticket.getPrice().doubleValue(); //convert BigDecimal to Double to match BIKE_RATE_PER_HOUR
		assertEquals((0 * Fare.BIKE_RATE_PER_HOUR), ticketPrice);
	}

	@Test
	public void calculateFareBikeWithLessThan30minutesParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (30 * 60 * 1000)); // 30 minutes parking time should give 0€ parking
																		// fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		
		ticketPrice = ticket.getPrice().doubleValue(); //convert BigDecimal to Double to match CAR_RATE_PER_HOUR
		assertEquals((0 * Fare.BIKE_RATE_PER_HOUR), ticketPrice);
	}

	@Test
	public void calculateFareCarWithDiscount() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // Remove an hour.
		Date outTime = new Date(); //Add out time
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime); //Put inTime to the ticket
		ticket.setOutTime(outTime); //Put ouTime to the ticket
		ticket.setParkingSpot(parkingSpot); //Put parkingSpot to the ticket
		fareCalculatorService.calculateFare(ticket, true);
		
		ticketPrice = ticket.getPrice().doubleValue(); //Get ticket price
		
		double discountCalculation = 1 * Fare.DISCOUNT_RATE * Fare.CAR_RATE_PER_HOUR; //Calculate expected price with discount.
		double expectedTicketPrice = Math.round(discountCalculation * Math.pow(10,  2)) / Math.pow(10, 2); //round to two digits after the decimal point
		
		assertEquals(expectedTicketPrice, ticketPrice);
	}

	@Test
	public void calculateFareBikeWithDiscount() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // Remove an hour.
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket, true);
		
		BigDecimal ticketPrice = new BigDecimal(ticket.getPrice().doubleValue());
		
		BigDecimal expectedPrice = new BigDecimal(1).multiply(new BigDecimal(Fare.DISCOUNT_RATE)).multiply(new BigDecimal(Fare.BIKE_RATE_PER_HOUR));
		BigDecimal expectedTicketPrice = expectedPrice.setScale(2, RoundingMode.DOWN);
		
		assertEquals(expectedTicketPrice, ticketPrice.setScale(2, RoundingMode.UP));
	}
}
