package co.edu.unimagdalena.finalbrasiliant.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.SeatCreateRequest;
import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.SeatResponse;
import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.SeatUpdateRequest;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Seat;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.SeatRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.SeatService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.SeatMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {
	private final SeatRepository seatRepo;
	private final BusRepository busRepo;
	private final SeatMapper seatMapper;
	
	@Transactional
	@Override
	public SeatResponse create(SeatCreateRequest request) {
		var bus = busRepo.findById(request.bus_id())
				.orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(request.bus_id())));
		Seat seat = seatMapper.toEntity(request);
		seat.setBus(bus);
		Seat saved = seatRepo.save(seat);
	    return seatMapper.toResponse(saved);
	}
	
	@Override
    public SeatResponse get(Long id) {
        return seatRepo.findById(id).map(seatMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("seat %d not found".formatted(id)));
    }
	
	@Override
    @Transactional
    public SeatResponse update(Long id, SeatUpdateRequest request) {
        var seat = seatRepo.findById(id).orElseThrow(() -> new NotFoundException("seat %d not found.".formatted(id)));
        if (request.bus_id() != null) {
            var bus = busRepo.findById(request.bus_id())
                .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(request.bus_id())));
            seat.setBus(bus);
        }
        seatMapper.patch(seat, request);
        return seatMapper.toResponse(seatRepo.save(seat));
    }
	
	@Override
    @Transactional
    public void delete(Long id) {
        seatRepo.deleteById(id);
    }
	
	@Override
	public List<SeatResponse> getSeatsByBus(Long bus_id){
		List<Seat> seats = seatRepo.findAllByBus_Id(bus_id);
		if(seats.isEmpty()) {
			throw new NotFoundException("No seats founds in bus %s".formatted(bus_id));
		}
		return seats.stream().map(seatMapper::toResponse).toList();
	}
	
	@Override
	public SeatResponse getSeatByNumberAndBus(String number, Long bus_id) {
		return seatRepo.findByNumberAndBus_Id(number, bus_id)
				.map(seatMapper::toResponse)
				.orElseThrow(() -> new NotFoundException("Seat %s wasn't found in bus %s".formatted(number, bus_id)));
	}
	
	@Override
	public List<SeatResponse> getSeatsByType(SeatType type){
		List<Seat> seats = seatRepo.findAllByType(type);
		if(seats.isEmpty()) {
			throw new NotFoundException("No seats founds with type %s".formatted(type));
		}
		return seats.stream().map(seatMapper::toResponse).toList();
	}
	
	@Override
	public SeatResponse getSeatByNumberAndTrip(String number, Long trip_id) {
		return seatRepo.findByNumberAndTrip_id(number, trip_id)
				.map(seatMapper::toResponse)
				.orElseThrow(() -> new NotFoundException("Seat %s wasn't found in trip %s".formatted(number, trip_id)));
	}
}
