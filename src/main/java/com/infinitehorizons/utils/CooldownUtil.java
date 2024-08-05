package com.infinitehorizons.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class CooldownUtil {

    private final Instant lastExecution;
    private final Instant nextExecution;

}
