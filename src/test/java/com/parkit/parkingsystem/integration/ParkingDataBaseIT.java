package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private Ticket ticket;
    private ParkingSpot parkingSpot;
    private double doubleValue;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
    	dataBasePrepareService.clearDataBaseEntries();
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ticket = new Ticket();
        
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar() throws Exception{
    	//TODO: check that a ticket is actually saved in DB and Parking table is updated with availability
    	//GIVEN: setUpPerTest()

    	//WHEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        //THEN
        String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber(); //Take vehicle's registration number from setUpPerTest()
        Ticket ticketDB = ticketDAO.getTicket(vehicleRegNumber); //Query vehicle number from database.
        assertThat(ticketDB.getVehicleRegNumber().equals(vehicleRegNumber)); //Check if ticket is same than the input.
        assertNotNull(ticketDB.getInTime()); //Check if database ticket inTime is added
        assertNull(ticketDB.getOutTime()); //Check if database ticket outTime is null
        assertThat(ticketDB.getPrice().equals(null)); //Check if database ticket price is null
        
		assertNotNull(ticketDAO.getTicket("ABCDEF")); //Check if vehicle's registration number exist.
		assertFalse(ticketDAO.getTicket("ABCDEF").getParkingSpot().isAvailable()); //Check if vehicle's parkingspot isn't available.
		
    }

    @Test
    public void testParkingLotExit() throws Exception{
    	//TODO: check that the fare generated and out time are populated correctly in the database
    	//GIVEN: setUpPerTest()
    	
    	//WHEN
    	testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        parkingService.setMinute(1);
        parkingService.processExitingVehicle();

        //THEN
        String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber(); //Take vehicle's registration number from setUpPerTest()      
        Ticket ticketDB = ticketDAO.getTicket(vehicleRegNumber); //Query vehicle number from database.
        assertThat(ticketDB.getVehicleRegNumber().equals(vehicleRegNumber)); //Check if ticket is same than the input.
        assertNotNull(ticketDB.getOutTime()); //Check if OutTime is not null
        
        doubleValue = ticketDB.getPrice().doubleValue(); //convert BigDecimal to Double to match CAR_RATE_PER_HOUR
        assertThat(ticketDB.getPrice().doubleValue()).isEqualTo(doubleValue); //Check if price is not null
        
    }
 
    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
    	//GIVEN: setUpPerTest()
    	String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
    	
    	//WHEN
       	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
       	
       	parkingService.setMinute(-120); //Set offset
        parkingService.processIncomingVehicle(); //Add new ticket
        parkingService.setMinute(-60); //Set offset
        parkingService.processExitingVehicle();
        
        parkingService.setMinute(-60); //Set offset
        parkingService.processIncomingVehicle();  //Add second ticket
        parkingService.setMinute(0); //Set offset
        parkingService.processExitingVehicle();
         
     	//THEN
        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber); //Take ticket    
        
        BigDecimal ticketPrice = ticket.getPrice();
        BigDecimal expectedPrice = new BigDecimal(1).multiply(new BigDecimal(Fare.DISCOUNT_RATE)).multiply(new BigDecimal(Fare.CAR_RATE_PER_HOUR)).setScale(2, RoundingMode.HALF_UP);
        
        assertThat(ticketPrice).isEqualTo(expectedPrice); 
            	
    }

}
