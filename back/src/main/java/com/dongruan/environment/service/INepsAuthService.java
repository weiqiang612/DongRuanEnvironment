package com.dongruan.environment.service;

import com.dongruan.environment.dto.NepsLoginRequest;
import com.dongruan.environment.dto.NepsLoginResponse;

public interface INepsAuthService {
    NepsLoginResponse authenticate(NepsLoginRequest request);
}
