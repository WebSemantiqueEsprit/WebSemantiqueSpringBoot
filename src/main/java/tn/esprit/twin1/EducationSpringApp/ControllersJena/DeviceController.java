package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.esprit.twin1.EducationSpringApp.servicesJena.DeviceService;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // Enable CORS for this controller
@RequestMapping("/devices") // Base URL for the controller
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    // Endpoint to get all devices
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDevices() {
        String result = deviceService.queryDevices();
        return ResponseEntity.ok(result);
    }

    // Endpoint to add a new device
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addDevice(@RequestBody Map<String, Object> newDevice) {
        String deviceName = (String) newDevice.get("deviceName");
        String powerRating = (String) newDevice.get("powerRating");
        String usageFrequency = (String) newDevice.get("usageFrequency");

        deviceService.addDevice(deviceName, powerRating, usageFrequency);
        return ResponseEntity.ok("Device added successfully!");
    }

    // Endpoint to update an existing device
    @PutMapping(value = "/{deviceName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateDevice(@PathVariable String deviceName,
                                               @RequestBody Map<String, Object> updatedDevice) {
        String newPowerRating = (String) updatedDevice.get("powerRating");
        String newUsageFrequency = (String) updatedDevice.get("usageFrequency");

        deviceService.updateDevice(deviceName, newPowerRating, newUsageFrequency);
        return ResponseEntity.ok("Device updated successfully!");
    }

    @DeleteMapping(value = "/{deviceName}")
    public ResponseEntity<String> deleteDevice(@PathVariable String deviceName) {
        deviceService.deleteDevice(deviceName);
        return ResponseEntity.ok("Device deleted successfully!");
    }

    // Endpoint to get devices by power rating
    @GetMapping(value = "/searchByPowerRating", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDeviceByPowerRating(@RequestParam("powerRating") String powerRating) {
        return deviceService.getDeviceByPowerRating(powerRating)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok("No devices found with the specified power rating."));
    }
    @GetMapping(value = "/filterByPowerRatingRange", produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<String> getDevicesByPowerRatingRange(
        @RequestParam("minPowerRating") String minPowerRating,
        @RequestParam("maxPowerRating") String maxPowerRating) {

    return deviceService.getDevicesByPowerRatingRange(minPowerRating, maxPowerRating)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.ok("No devices found within the specified power rating range."));
}

}
