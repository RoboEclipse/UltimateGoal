package org.firstinspires.ftc.teamcode.drive;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.localization.ThreeTrackingWheelLocalizer;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.util.Encoder;

import java.util.Arrays;
import java.util.List;

import static org.firstinspires.ftc.teamcode.drive.DriveConstants.testRobot;

/*
 * Sample tracking wheel localizer implementation assuming the standard configuration:
 *
 *    /--------------\
 *    |     ____     |
 *    |     ----     |
 *    | ||        || |
 *    | ||        || |
 *    |              |
 *    |              |
 *    \--------------/
 *
 */
@Config
public class StandardTrackingWheelLocalizer extends ThreeTrackingWheelLocalizer {

    public static double TICKS_PER_REV = 8192;
    public static double WHEEL_RADIUS = 1; // in
    public static double GEAR_RATIO = 1; // output (wheel) speed / input (encoder) speed

    public static double LATERAL_DISTANCE = 13.678; //13.7133; // in; distance between the left and right wheels
    public static double FORWARD_OFFSET = -4.75; // in; offset of the lateral wheel

    public static double rightWheelRatio = 0.993;


    private Encoder leftEncoder, rightEncoder, frontEncoder;

    public static void localizerSwitchToTestRobot(){
        TICKS_PER_REV = testBotConstantStorage.TicksPerRev;
        WHEEL_RADIUS = testBotConstantStorage.wheelRadius;
        GEAR_RATIO = 1; // output (wheel) speed / input (encoder) speed

        LATERAL_DISTANCE = testBotConstantStorage.lateralDistance; // in; distance between the left and right wheels
        FORWARD_OFFSET = testBotConstantStorage.forwardOffset; // in; offset of the lateral wheel
    }

    public StandardTrackingWheelLocalizer(HardwareMap hardwareMap) {
        super(Arrays.asList(
                new Pose2d(0, LATERAL_DISTANCE / 2, 0), // left
                new Pose2d(0, -LATERAL_DISTANCE / 2, 0), // right
                new Pose2d(FORWARD_OFFSET , 0, Math.toRadians(90)) // front
        ));

        leftEncoder = new Encoder(hardwareMap.get(DcMotorEx.class, "lb"));
        rightEncoder = new Encoder(hardwareMap.get(DcMotorEx.class, "rb"));
        frontEncoder = new Encoder(hardwareMap.get(DcMotorEx.class, "lf"));


        // TODO: reverse any encoders using Encoder.setDirection(Encoder.Direction.REVERSE)
        /*if(testRobot){
            localizerSwitchToTestRobot();
            DriveConstants.constantsSwitchToTestRobot();
            frontEncoder.setDirection(Encoder.Direction.REVERSE);
        }*/

    }

    public static double encoderTicksToInches(double ticks) {
        return WHEEL_RADIUS * 2 * Math.PI * GEAR_RATIO * ticks / TICKS_PER_REV;
    }

    @NonNull
    @Override
    public List<Double> getWheelPositions() {
        return Arrays.asList(
                encoderTicksToInches(leftEncoder.getCurrentPosition()),
                encoderTicksToInches(rightEncoder.getCurrentPosition() * rightWheelRatio),
                encoderTicksToInches(frontEncoder.getCurrentPosition())
        );
    }

    @NonNull
    @Override
    public List<Double> getWheelVelocities() {
        // TODO: If your encoder velocity can exceed 32767 counts / second (such as the REV Through Bore and other
        //  competing magnetic encoders), change Encoder.getRawVelocity() to Encoder.getCorrectedVelocity() to enable a
        //  compensation method

        return Arrays.asList(
                encoderTicksToInches(leftEncoder.getCorrectedVelocity()),
                encoderTicksToInches(rightEncoder.getCorrectedVelocity() * rightWheelRatio),
                encoderTicksToInches(frontEncoder.getCorrectedVelocity())
        );
    }
}
