package com.ee472.daniel.supertank;

public interface JoystickMovedListener {

    public void OnMoved(int pan, int tilt);

    public void OnHold();

    public void OnReleased();

}
