package io.github.pocketrice;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;

import java.util.Map;

public class Force {
    static final float EARTH_G = 9.8f;
    Vector3 forceVec;


    public Force(Vector3 vec) {
        forceVec = vec;
    }


    // F = ma -> a = F/m
    public void apply(Rigidbody rb) {
        rb.addAcceleration(forceVec.scl(1f / rb.getMass()));
    }

    public static Force gravForce(float g) {
        return new Force(new Vector3(0f, -g, 0f));
    }

    public Vector3 getVec() {
        return forceVec;
    }

}
