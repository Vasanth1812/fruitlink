package com.fruitlink.settings.repository;

import com.fruitlink.settings.entity.GlobalSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlobalSettingRepository extends JpaRepository<GlobalSetting, String> {
}
