package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;
    private Calendar calendar;
    private int minute;
    

	public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO){
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
        this.minute = 0;
    }

    public void processIncomingVehicle() {
        try{
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if(parkingSpot != null && parkingSpot.getId() > 0){
                String vehicleRegNumber = getVehichleRegNumber();
                if(ticketDAO.getRegAlreadyPark(vehicleRegNumber)>0) {//Check if any vehicle having the same REG is already park.
                	System.out.println("Erreur système : La plaque d'immatriculation renseignée est déjà garée.");
                }else {
	                parkingSpot.setAvailable(false);
	                parkingSpotDAO.updateParking(parkingSpot);//allot this parking space and mark it's availability as false
	                
	                calendar = Calendar.getInstance(); //Initialize calendar with time.
	                calendar.add(Calendar.MINUTE,minute); //Adding offset.
	                Date inTime = calendar.getTime(); //Getting time from calendar.
	                Ticket ticket = new Ticket();
	                //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
	                //ticket.setId(ticketID);
	                ticket.setParkingSpot(parkingSpot);
	                ticket.setVehicleRegNumber(vehicleRegNumber);
	                ticket.setPrice(BigDecimal.ZERO);
	                ticket.setInTime(inTime);
	                ticket.setOutTime(null);
	                ticketDAO.saveTicket(ticket);
	                int nbTicket;
	                nbTicket = ticketDAO.getNbTicket(vehicleRegNumber);
	                
	                if(nbTicket>1) {
	                	System.out.println("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%");
	                }
	                
	                System.out.println("Generated Ticket and saved in DB");
	                System.out.println("Please park your vehicle in spot number:"+parkingSpot.getId());
	                System.out.println("Recorded in-time for vehicle number:"+vehicleRegNumber+" is:"+inTime);
                }
            }
        }catch(Exception e){
            logger.error("Unable to process incoming vehicle",e);
        }
    }

    private String getVehichleRegNumber() throws Exception {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    public ParkingSpot getNextParkingNumberIfAvailable(){
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        try{
            ParkingType parkingType = getVehichleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if(parkingNumber > 0){
                parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
            }else{
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        }catch(IllegalArgumentException ie){
            logger.error("Error parsing user input for type of vehicle", ie);
        }catch(Exception e){
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    private ParkingType getVehichleType(){
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch(input){
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    public void processExitingVehicle() {
        try{
            String vehicleRegNumber = getVehichleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
            
            
            calendar = Calendar.getInstance(); //Initialize calendar with time.
            calendar.add(Calendar.MINUTE,minute); //Adding offset.
            Date outTime = calendar.getTime();//Getting time from calendar.
            
            ticket.setOutTime(outTime);
            
       
            int nbSameReg = ticketDAO.getNbTicket(vehicleRegNumber);
            Boolean applyDiscount = false;
            
            if(nbSameReg>1) {
            	applyDiscount = true;
            }
            
            fareCalculatorService.calculateFare(ticket,applyDiscount);
            if(ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
                if(applyDiscount && ticket.getPrice().compareTo(BigDecimal.ZERO)>0) {
                	System.out.println("Special 5% reduction has been applied");
                }
               
                System.out.println("Please pay the parking fare: " + ticket + "€.");
                System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
            }else{
                System.out.println("Unable to update ticket information. Error occurred");
            }
        }catch(Exception e){
            logger.error("Unable to process exiting vehicle",e);
        }
    }

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}
	
    public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}
}
