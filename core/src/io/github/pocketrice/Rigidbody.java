package io.github.pocketrice;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Array;

public class Rigidbody {
    ModelInstance mi;
    btCollisionShape colShape;
    Array<Force> forces;
    Vector3 location, velocity, acceleration;
    float mass;


    public Rigidbody(Model model, btCollisionShape cs, float m, Force... f) {
        mi = new ModelInstance(model);
        colShape = cs;
        mass = m;

        forces = new Array<Force>();
        forces.addAll(f);
        location = Vector3.Zero;
        velocity = Vector3.Zero;
        acceleration = Vector3.Zero;
    }

    public float getMass() {
        return mass;
    }

    public Vector3 getLocation() {
        return location;
    }

    public void addVelocity(Vector3 vec3) {
        velocity.add(vec3);
    }

    public void addAcceleration(Vector3 vec3) {
        acceleration.add(vec3);
    }

    public void update() {
        forces.forEach(f -> f.apply(this));
        velocity.add(acceleration);

    }
}
