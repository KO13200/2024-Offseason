// Copyright (c) 2024 CurtinFRC
// Open Source Software, you can modify it according to the terms
// of the MIT License at the root of this project

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants;
import edu.wpi.first.wpilibj.DataLogManager; 
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.util.datalog.BooleanLogEntry;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.util.datalog.StringLogEntry;

/** Our Crescendo shooter Subsystem */
public class Shooter extends SubsystemBase {
  private PIDController m_pid;
  private CANSparkMax m_motor;
  private RelativeEncoder m_encoder;
  private DataLog log = DataLogManager.getLog();
 private DoubleLogEntry shooter_encoder_vel = new DoubleLogEntry(log, "/shooter/encoder/velocity");
 private DoubleLogEntry shooter_encoder_pos = new DoubleLogEntry(log, "/shooter/encoder/position");

  /**
   * Creates a new {@link Shooter} {@link edu.wpi.first.wpilibj2.command.Subsystem}.
   *
   * @param motor The motor that the shooter controls.
   */
  public Shooter(CANSparkMax motor) {
    m_motor = motor;
    m_encoder = m_motor.getEncoder();
    shooter_encoder_pos.append(m_encoder.getPosition());
    shooter_encoder_vel.append(m_encoder.getVelocity());
    m_pid = new PIDController(Constants.shooterP, Constants.shooterI, Constants.shooterD);
  }

  /** Acheives and maintains speed. */
  private Command achieveSpeeds(double speed) {
    m_pid.reset();
    m_pid.setSetpoint(speed);
    return Commands.run(
        () ->
            m_motor.setVoltage(
                m_pid.calculate(
                    -1 * Units.rotationsPerMinuteToRadiansPerSecond(m_encoder.getVelocity()))));
  }

  /**
   * Speeds up the shooter until it hits the specified speed then stops.
   *
   * @param speed The desired speed in Radians per second.
   * @return a {@link Command} to get to the desired speed.
   */
  public Command spinup(double speed) {
    return achieveSpeeds(speed).until(m_pid::atSetpoint);
  }

  public Command stop() {
    return runOnce(() -> m_motor.set(0));
  }

  /**
   * Holds the shooter at the current speed setpoint.
   *
   * @return A {@link Command} to hold the speed at the setpoint.
   */
  public Command maintain() {
    return achieveSpeeds(m_pid.getSetpoint());
  }

  /**
   * Checks if the Shooter is at its setpoint and the loop is stable.
   *
   * @return A {@link Trigger} from the result.
   */
  public Trigger atSetpoint() {
    return new Trigger(
        () ->
            m_pid.getSetpoint()
                    == Units.rotationsPerMinuteToRadiansPerSecond(m_encoder.getVelocity())
                && m_pid.atSetpoint());
  }
}
