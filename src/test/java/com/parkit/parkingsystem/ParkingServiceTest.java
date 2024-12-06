package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
  
    private Ticket ticket;

    @BeforeEach
    private void setUpPerTest() {
        try {
        	
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,true);
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
           
                     
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }
    
    @Test
    public void processIncomingVehicleTest() {
    	
    	//GIVEN
    	when(inputReaderUtil.readSelection()).thenReturn(1);//set Car
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);//set parkingNumber to 1 to simulate availability
    	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    	    	
    	//WHEN
    	parkingService.processIncomingVehicle();
    	
    	//THEN
    	verify(ticketDAO).saveTicket(any(Ticket.class));
    	
    } 
  

    @Test
    public void processExitingVehicleTest() throws Exception{
    	//GIVEN
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
    	when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
    	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
    	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    	
    	
    	//WHEN
        parkingService.processExitingVehicle();
        
    	//THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

    }
    
    @Test
    public void processExitingVehicleTestWithDiscount() throws Exception{
    	//GIVEN
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
    	when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
    	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
    	
    	//WHEN
        parkingService.processExitingVehicle();
        
    	//THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

    }
    
    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception{
    	//GIVEN
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
    	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);//set false
    	  
    	//WHEN
        parkingService.processExitingVehicle();
        
    	//THEN
        verify(ticketDAO).updateTicket(any(Ticket.class));

    }
    
    @Test
    public void testGetNextParkingNumberIfAvailable(){
    	//GIVEN.
    	when(inputReaderUtil.readSelection()).thenReturn(1);									//set Car for test
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1); 		//Get getNextAvailableSlot by putting any ParkingType and return "1"
    	
    	//WHEN
    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(); 			// Call getNextParkingNumberIfAvailable() and return result to variable that will be checked
    	
    	//THEN
        assertEquals(1, parkingSpot.getId()); 													//Check if parkingSpot's ID = 1
        assertEquals(true, parkingSpot.isAvailable()); 											//Check if parkingSpot's availability is true
        assertEquals(ParkingType.CAR, parkingSpot.getParkingType());							//Check if parkingType is ParkingType.CAR from variable.
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));	//Check if parkingSpotDAO call getNextAvailableSlot() once
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
    	//GIVEN.
    	when(inputReaderUtil.readSelection()).thenReturn(1);									//set Car for test
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0); 		//Get getNextAvailableSlot by putting any ParkingType and return "0" to simulate parkingspot not found.
    	
    	//WHEN
    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(); 			//Call getNextParkingNumberIfAvailable() and return result to variable that will be checked
    	
    	//THEN
    	assertEquals(null, parkingSpot);														//Check if parkingSpot is null
    	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));	//Check if parkingSpotDAO call getNextAvailableSlot() once
    	
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
    	//GIVEN
    	when(inputReaderUtil.readSelection()).thenReturn(3);									//set wrong value (no car, no bike)
    	
    	//WHEN
    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(); 			//Call getNextParkingNumberIfAvailable() and return result to variable that will be checked
    	
    	//THEN
    	assertEquals(null, parkingSpot);														//Check if parkingSpot is null
    }

}
