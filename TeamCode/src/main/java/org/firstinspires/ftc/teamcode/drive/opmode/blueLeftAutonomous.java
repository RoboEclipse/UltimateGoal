package org.firstinspires.ftc.teamcode.drive.opmode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.UltimateGoal.AutoTransitioner;
import org.firstinspires.ftc.teamcode.UltimateGoal.AutonomousMethods;
import org.firstinspires.ftc.teamcode.UltimateGoal.Constants;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;

import java.util.Vector;

@Config
@Autonomous(group = "drive")
public class blueLeftAutonomous extends AutonomousMethods {
    private FtcDashboard dashboard;

    // String for holding detection
    String detection = "";

    @Override
    public void runOpMode() {

        initializeAutonomousAttachments(hardwareMap, telemetry);
        telemetry.addData("Initialization", "In Progress");
        telemetry.update();

        boolean isRed = false;

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        setWobbleClaw(true);
        prepElevator();

        initVuforia();
        initTfod();

        dashboard = FtcDashboard.getInstance();
        dashboard.startCameraStream(tfod, 0);

        if (tfod != null) { //Maybe move into Autonomous Methods
            tfod.activate();
        }

        //Create Vectors and Poses
        Pose2d startPose = new Pose2d(-63, 55.5, Math.toRadians(0));
        drive.setPoseEstimate(startPose);
        Vector2d firstDropPositionClose = new Vector2d(5,58);
        //Was 27 30
        Vector2d firstDropPositionMid = new Vector2d(25,25);
        Vector2d firstDropPositionFar = new Vector2d(50,50);
        Vector2d preShootVector = new Vector2d(-30, 55.5);
        Vector2d shootVector = new Vector2d(-6, 34);
        Vector2d secondGrabPositionClose = new Vector2d(-42, 15);
        Vector2d secondGrabPositionMid = new Vector2d(-39, 14.2);
        Vector2d secondGrabPositionFar = new Vector2d(-41, 20);
        //Vector2d secondDropPosition = firstDropPosition.plus(new Vector2d(-5,3));
        Vector2d secondDropPositionClose = firstDropPositionClose.plus(new Vector2d(-10,-10)); //new Vector2d(-1.5, 57)
        Vector2d secondDropPositionMid = firstDropPositionMid.plus(new Vector2d(-12,-3));
        Vector2d secondDropPositionFar = firstDropPositionFar.plus(new Vector2d(-9,3));

        //Generate constant trajectories
        Trajectory toShoot = drive.trajectoryBuilder(startPose)
                .addTemporalMarker(0, () -> {
                    //raiseWobble();
                    setWobbleClaw(true);
                    setShooterAngle(Constants.setShooterAngle);
                })
                .splineToConstantHeading(preShootVector, 0) //Goes right in front of the ring
                .addTemporalMarker(1.5, () -> {
                    prepShooter();
                })
                //.splineToConstantHeading(new Vector2d(-30, 60), 0)
                //.splineToConstantHeading(new Vector2d(-12, 60), 0)
                .splineToConstantHeading(shootVector, -90) // Goes to shooting position
                .build();
        //Generate variable trajectory sets
        Trajectory[] closeTrajectories = generateRoute(drive, firstDropPositionClose, secondGrabPositionClose, secondDropPositionClose);
        Trajectory[] midTrajectories = generateRoute(drive, firstDropPositionMid, secondGrabPositionMid, secondDropPositionMid);
        Trajectory[] farTrajectories = generateRoute(drive, firstDropPositionFar, secondGrabPositionFar, secondDropPositionFar);
        Trajectory[] driveTrajectories;

        telemetry.addData("Initialization", "Finished");
        telemetry.addData("LeftSensor", autonomousGetLeftDistance());
        telemetry.addData("FrontSensor", autonomousGetFrontDistance());
        telemetry.update();

        waitForStart();
        //Go to shoot location and power up shooter motor
        drive.followTrajectory(toShoot);
        //Correct imu
        //TODO: Replace with refreshPose
        encoderTurn(0,1,3);
        drive.setPoseEstimate(new Pose2d(shootVector, 0));
        //Shoot
        sleep(1008);
        shootRings();
        hoverWobble();
        //getWobbleDropPose
        detection = getWobbleDropPose();
        //Set trajectories based on ring detection
        if (detection.equals("Quad")) {
            driveTrajectories = farTrajectories;
        } else if (detection.equals("Single")) {
            driveTrajectories = midTrajectories;
        } else {
            driveTrajectories = closeTrajectories;
        }
        //Drive to first goal drop position
        drive.followTrajectory(driveTrajectories[0]);
        //Drop first goal
        setWobbleClaw(false);
        sleep(500);

        //Drive to second goal pickup location and reset angle
        drive.followTrajectory(driveTrajectories[1]);
        encoderTurn(120,1,3);

        //drive.followTrajectory(driveTrajectories[2]);
        lowerWobble();
        sleep(120);
        autoAdjust(8.1, drive);
        //Pick up second goal
        sleep(500);
        setWobbleClaw(true);
        sleep(500);
        hoverWobble();
        //Readjust
        double currentHeading = getHorizontalAngle();
        drive.setPoseEstimate(new Pose2d(driveTrajectories[2].end().vec(), Math.toRadians(currentHeading)));
        //Drive to second goal drop position
        drive.followTrajectory(driveTrajectories[3]);

        //Drop second goal
        setWobbleClaw(false);
        sleep(200);
        raiseWobble();
        sleep(500);

        //Drive backwards
        drive.followTrajectory(driveTrajectories[4]);

        //Park
        drive.followTrajectory(driveTrajectories[5]);
        encoderTurn(0,1,2);

        if (tfod != null) {
            tfod.shutdown();
        }
        AutoTransitioner.transitionOnStop(this, "TeleOp");
    }

    private Trajectory[] generateRoute(SampleMecanumDrive drive, Vector2d firstDropPosition, Vector2d secondGrabPosition, Vector2d secondDropPosition){
        Trajectory[] output = new Trajectory[6];
        //TODO: UPDATE THE SHOOT VECTOR BECAUSE IT'S WRONG
        Vector2d shootVector = new Vector2d(-5, 30);

        //Vector2d secondDropPosition = firstDropPosition.plus(new Vector2d(-5,3));

        Vector2d parkPosition = new Vector2d(11.5, 22);
        Pose2d potentialParkPosition = new Pose2d(-2, 8, Math.toRadians(9));

        Trajectory dropFirstWobble = drive.trajectoryBuilder(new Pose2d(shootVector, 0), 0) //Start at shoot position
                .strafeTo(firstDropPosition) //Go to firstDropPosition
                .build();
        Trajectory getSecondWobble = drive.trajectoryBuilder(dropFirstWobble.end())
                .back(10)
                .splineToConstantHeading(new Vector2d(0, 38), 0)
                //.splineToConstantHeading(new Vector2d(-6, 38), 0)
                .splineTo(new Vector2d(7, 25), Math.toRadians(-90))
                //.splineTo(new Vector2d(-16, 12), Math.toRadians(-180))
                .splineTo(secondGrabPosition.plus(new Vector2d(7, -7.3)), Math.toRadians(120))
                .build();
        Trajectory toSecondWobble = drive.trajectoryBuilder(getSecondWobble.end())
                .lineToConstantHeading(secondGrabPosition)
                .build();
        Trajectory dropSecondWobble = drive.trajectoryBuilder(toSecondWobble.end())
                .splineToSplineHeading(new Pose2d(-48, 48, Math.toRadians(30)), Math.toRadians(90))
                .splineToSplineHeading(new Pose2d(-36, 57, Math.toRadians(0)), Math.toRadians(0))
                .splineTo(secondDropPosition, Math.toRadians(0))
                .build();
        Trajectory backing = drive.trajectoryBuilder(dropSecondWobble.end())
                .back(5)
                .build();
        Trajectory park = drive.trajectoryBuilder(backing.end())
                .addTemporalMarker(0, () -> {
                    setWobbleMotorPosition(0.9, 0);
                })
                //.strafeTo(parkPosition)
                .strafeTo(potentialParkPosition.vec())
                .addDisplacementMarker(24, () -> {
                    setRingBlocker(Constants.rightSideArmStraight);
                })
                .build();

        output[0] = dropFirstWobble;
        output[1] = getSecondWobble;
        output[2] = toSecondWobble;
        output[3] = dropSecondWobble;
        output[4] = backing;
        output[5] = park;
        return output;
    }
}
