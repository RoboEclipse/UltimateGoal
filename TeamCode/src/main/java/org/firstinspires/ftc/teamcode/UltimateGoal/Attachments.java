package org.firstinspires.ftc.teamcode.UltimateGoal;

import android.graphics.Bitmap;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.baseBot.Drivetrain;

public class Attachments extends Drivetrain {
    private Telemetry telemetry;
    private ElapsedTime runtime = new ElapsedTime();
    public Configuration names = new Configuration();
    public DcMotor collectionMotor, wobbleGoalMotor, leftFlywheelMotor, rightFlywheelMotor;
    public Servo wobbleGoalServo, ringPushServo, elevatorServo, tiltServo;

    //Backend
    void initialize(HardwareMap hardwareMap, Telemetry telemetry_){
        telemetry = telemetry_;
        FtcDashboard dashboard = FtcDashboard.getInstance();

        // Motors
        collectionMotor = hardwareMap.dcMotor.get(names.collectionMotor);
        wobbleGoalMotor = hardwareMap.dcMotor.get(names.wobbleGoalMotor);
        leftFlywheelMotor = hardwareMap.dcMotor.get(names.leftShooterMotor);
        rightFlywheelMotor = hardwareMap.dcMotor.get(names.rightShooterMotor);

        // Servos
        wobbleGoalServo = hardwareMap.servo.get(names.wobbleGoalServo);
        ringPushServo = hardwareMap.servo.get(names.ringPushServo);
        elevatorServo = hardwareMap.servo.get(names.elevatorServo);
        tiltServo = hardwareMap.servo.get(names.tiltServo);

        // Motor initalization
        collectionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        wobbleGoalMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftFlywheelMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFlywheelMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftFlywheelMotor.setDirection(DcMotor.Direction.REVERSE);
        rightFlywheelMotor.setDirection(DcMotor.Direction.FORWARD);
        leftFlywheelMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFlywheelMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        collectionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        wobbleGoalMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //HardwareMaps
        initializeDriveTrain(hardwareMap, telemetry_);
    }

    void setWobbleClaw(double position) {
        wobbleGoalServo.setPosition(position);
    }

    void runFlywheelMotor(double power) {
        leftFlywheelMotor.setPower(-power);
        rightFlywheelMotor.setPower(power);
    }

    /* void runFlywheelMotor(double power) {
        flywheelMotor.setPower(power);
    }*/

    /* void setRingPusher(double position) {
        ringPusher.setPosition(position);
    } */
}
