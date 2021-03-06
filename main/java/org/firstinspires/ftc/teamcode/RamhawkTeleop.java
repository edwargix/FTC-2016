/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "Ramhawk Teleop", group = "Main")
public class RamhawkTeleop extends LinearOpMode {
    // Hardware
    private RamhawkHardware robot = new RamhawkHardware();

    @Override
    public void runOpMode() {
        double left;
        double right;
        double max;

        // Get hardware ready
        robot.init(hardwareMap);

        // Robot Controller's layout
        final View relativeLayout = ((Activity) robot.hwMap.appContext).findViewById(R.id.RelativeLayout);

        // Greet the driver
        telemetry.addData("Say", "Hello Driver");    //
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // Vars for keeping track of the state of LED
        boolean colorLedCurrentState;
        boolean colorLedPreviousState = false;

        // Run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            // Get joystick input from driver
            left = -gamepad1.left_stick_y + gamepad1.right_stick_x;
            right = -gamepad1.left_stick_y - gamepad1.right_stick_x;

            // Normalize the values so neither exceed +/- 1.0
            max = Math.max(Math.abs(left), Math.abs(right));
            if (max > 1.0) {
                left /= max;
                right /= max;
            }

            /* // Temporarily remove distance sensor stopping movement
            if (robot.distanceSensor.getRawLightDetected() > 0.05) {
                if (left > 0) left = 0;
                if (right > 0) right = 0;
                telemetry.addData("Greater than 0.4?", "yes");
            } else
                telemetry.addData("Greater than 0.4?", "no");*/

            robot.leftMotor.setPower(left);
            robot.rightMotor.setPower(right);

            // Use gamepad buttons to move arm up (Y) and down (A)
            if (gamepad1.y) {
                robot.armMotor1.setPower(RamhawkHardware.ARM_UP_POWER);
                // robot.armMotor2.setPower(RamhawkHardware.ARM_UP_POWER);
            } else if (gamepad1.a) {
                robot.armMotor1.setPower(RamhawkHardware.ARM_DOWN_POWER);
                // robot.armMotor2.setPower(RamhawkHardware.ARM_DOWN_POWER);
            } else {
                robot.armMotor1.setPower(0.0);
                // robot.armMotor2.setPower(0.0);
            }

            colorLedCurrentState = gamepad1.x;

            // check for button state transitions.
            if (colorLedCurrentState && !colorLedPreviousState) {
                // button is transitioning to a pressed state. So Toggle LED
                robot.ledOn = !robot.ledOn;
                robot.colorSensor.enableLed(robot.ledOn);
            }

            colorLedPreviousState = colorLedCurrentState;

            Color.RGBToHSV(robot.colorSensor.red() * 8, robot.colorSensor.green() * 8, robot.colorSensor.blue() * 8, robot.hsvValues);

            // Data relevant to drive
            telemetry.addData("left", "%.2f", left);
            telemetry.addData("right", "%.2f", right);

            // Data Relevant to distance sensor
            telemetry.addData("Raw", robot.distanceSensor.getRawLightDetected());
            telemetry.addData("Normal", robot.distanceSensor.getLightDetected());
            telemetry.addData("Raw Max", robot.distanceSensor.getRawLightDetectedMax());

            // Data relevant to LED
            telemetry.addData("LED", robot.ledOn ? "On" : "Off");
            telemetry.addData("Clear", robot.colorSensor.alpha());
            telemetry.addData("Red  ", robot.colorSensor.red());
            telemetry.addData("Green", robot.colorSensor.green());
            telemetry.addData("Blue ", robot.colorSensor.blue());
            telemetry.addData("Hue", robot.hsvValues[0]);
            telemetry.addData("Saturation", robot.hsvValues[1]);
            telemetry.addData("Value", robot.hsvValues[2]);

            // Change Robot Controller's background color to color sensor's readings
            relativeLayout.post(new Runnable() {
                public void run() {
                    relativeLayout.setBackgroundColor(Color.HSVToColor(0xff, robot.hsvValues));
                }
            });

            // Send data to driver
            telemetry.update();

            // Pause for metronome tick.  40 mS each cycle = update 25 times a second.
            robot.waitForTick(40);
        }
    }
}
