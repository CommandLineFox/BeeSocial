package raf.aleksabuncic.types;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Token za Suzuki-Kasami algoritam.
 */
@Getter
public class Token implements Serializable {
    public final Map<Integer, Integer> LN = new HashMap<>();
    public final Queue<Integer> queue = new LinkedList<>();

    @Override
    public String toString() {
        return "Token{" + "LN=" + LN + ", queue=" + queue + '}';
    }
}
