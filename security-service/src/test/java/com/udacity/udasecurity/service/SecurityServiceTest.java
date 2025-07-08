package com.udacity.udasecurity.service;

import com.udacity.ImageService.service.ImageService;
import com.udacity.udasecurity.application.StatusListener;
import com.udacity.udasecurity.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;


public class SecurityServiceTest {
    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private ImageService imageService;

    private SecurityService securityService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        securityService = new SecurityService(securityRepository, imageService);

    }

    @Test
    public void whenAlarmIsArmedAndSensorActivated_thenSystemGoesToPendingAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(new Sensor("Front Door", SensorType.DOOR), true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    public void whenAlarmIsPendingAndSensorActivated_thenSystemGoesToAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(new Sensor("Back Window", SensorType.WINDOW), true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void whenPendingAlarmAndAllSensorsInactive_thenSystemReturnsToNoAlarm() {
        Sensor sensor = mock(Sensor.class);
        when(sensor.getActive()).thenReturn(true); // Sensor starts as active
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getSensors()).thenReturn(Set.of(sensor));

        securityService.changeSensorActivationStatus(sensor, false); // Deactivating the sensor

        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.ALARM); // Should NOT go to ALARM
        verify(securityRepository, times(1)).updateSensor(sensor); // Ensure sensor update happens
    }

    @Test
    public void whenAlarmIsActive_changeInSensorStateShouldNotAffectAlarm() {
        Sensor sensor = mock(Sensor.class);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, true); // Activate sensor
        securityService.changeSensorActivationStatus(sensor, false); // Deactivate sensor

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class)); // Alarm should NOT change
    }

    @Test
    public void whenSensorActivatedWhileAlreadyActiveAndPendingAlarm_thenSetToAlarm() {
        Sensor sensor = mock(Sensor.class);

        when(sensor.getActive()).thenReturn(false, true);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void whenSensorDeactivatedWhileAlreadyInactive_thenNoChangeInAlarmState() {
        Sensor sensor = mock(Sensor.class);
        when(sensor.getActive()).thenReturn(false);

        AlarmStatus currentStatus = AlarmStatus.NO_ALARM;
        when(securityRepository.getAlarmStatus()).thenReturn(currentStatus);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    public void whenCatDetectedWhileSystemArmedHome_thenSetToAlarm() {
        BufferedImage mockImage = mock(BufferedImage.class);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        securityService.processImage(mockImage);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void whenNoCatDetectedAndNoActiveSensors_thenSetToNoAlarm() {
        BufferedImage mockImage = mock(BufferedImage.class);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(false);
        when(securityRepository.getSensors()).thenReturn(new HashSet<>()); // No active sensors

        securityService.processImage(mockImage);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }


    @ParameterizedTest
    @EnumSource(ArmingStatus.class)
    public void armingStatus_isSetCorrectlyForAllStates(ArmingStatus status) {
        securityService.setArmingStatus(status);
        verify(securityRepository).setArmingStatus(status);
    }

    @Test
    public void whenSystemIsArmed_thenResetAllSensorsToInactive() {
        Sensor sensor1 = mock(Sensor.class);
        Sensor sensor2 = mock(Sensor.class);
        when(sensor1.getActive()).thenReturn(true);
        when(sensor2.getActive()).thenReturn(true);

        Set<Sensor> sensors = new HashSet<>(Set.of(sensor1, sensor2));
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);

        verify(sensor1).setActive(false);
        verify(sensor2).setActive(false);
    }

    @Test
    void settingArmingStatusToArmedShouldResetAllSensorsToInactive() {
        Sensor sensor1 = new Sensor("Front Door", SensorType.DOOR);
        Sensor sensor2 = new Sensor("Back Window", SensorType.WINDOW);
        sensor1.setActive(true);
        sensor2.setActive(true);

        Set<Sensor> sensors = Set.of(sensor1, sensor2);
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);

        sensors.forEach(sensor -> assertFalse(sensor.getActive()));
        verify(securityRepository).updateSensor(sensor1);
        verify(securityRepository).updateSensor(sensor2);
    }
    @Test
    public void whenSystemIsDisarmed_thenSetToNoAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }


    //some more
    @Test
    public void whenSensorInactiveDuringArming_thenDoNotResetSensor() {
        Sensor sensor = mock(Sensor.class);
        when(sensor.getActive()).thenReturn(false);
        Set<Sensor> sensors = Set.of(sensor);
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(sensor, never()).setActive(false);
        verify(securityRepository, never()).updateSensor(sensor);
    }

    @Test
    public void whenNoCatDetectedAndArmedHome_thenSetToNoAlarm() {
        BufferedImage mockImage = mock(BufferedImage.class);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(false);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        securityService.processImage(mockImage);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    public void addStatusListener_shouldNotifyOnCatDetection() {
        StatusListener listener = mock(StatusListener.class);
        securityService.addStatusListener(listener);

        BufferedImage mockImage = mock(BufferedImage.class);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        securityService.processImage(mockImage);

        verify(listener).catDetected(true);
        verify(listener).notify(AlarmStatus.ALARM);
    }

    @Test
    public void removeStatusListener_shouldNotNotifyAfterRemoval() {
        StatusListener listener = mock(StatusListener.class);
        securityService.addStatusListener(listener);
        securityService.removeStatusListener(listener);

        BufferedImage mockImage = mock(BufferedImage.class);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        securityService.processImage(mockImage);

        verify(listener, never()).catDetected(anyBoolean());
        verify(listener, never()).notify(any());
    }

    @Test
    public void whenSensorActivatedAndSystemDisarmed_thenNoAlarmChange() {
        Sensor sensor = mock(Sensor.class);
        when(sensor.getActive()).thenReturn(false);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    public void whenSensorDeactivatedAndAlarmIsAlarm_thenSetToPendingAlarm() {
        Sensor sensor = mock(Sensor.class);
        when(sensor.getActive()).thenReturn(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    public void testGetAlarmStatus() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        AlarmStatus status = securityService.getAlarmStatus();
        assertEquals(AlarmStatus.ALARM, status);
    }

    @Test
    public void testAddSensor() {
        Sensor sensor = new Sensor("Test Sensor", SensorType.DOOR);
        securityService.addSensor(sensor);
        verify(securityRepository).addSensor(sensor);
    }

    @Test
    public void testRemoveSensor() {
        Sensor sensor = new Sensor("Test Sensor", SensorType.DOOR);
        securityService.removeSensor(sensor);
        verify(securityRepository).removeSensor(sensor);
    }

}
