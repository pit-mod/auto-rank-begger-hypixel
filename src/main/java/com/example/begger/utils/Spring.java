package com.example.begger.utils;

public class Spring {
    public float value;
    public float velocity;

    public Spring(float val) {
        this.value = val;
    }

    public void update(float target, float stiffness, float damping) {
        float dt = 0.0166f;
        float force = stiffness * (target - value) - damping * velocity;
        velocity += force * dt;
        value += velocity * dt;
    }
}
