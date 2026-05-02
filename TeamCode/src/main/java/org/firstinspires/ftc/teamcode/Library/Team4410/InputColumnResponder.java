package org.firstinspires.ftc.teamcode.Library.Team4410;

import java.util.function.Supplier;

public interface InputColumnResponder {
  InputColumnResponder register(Supplier<Boolean> predicate, Runnable triggerCallback);
  void update();
  void clearRegistry();
}
