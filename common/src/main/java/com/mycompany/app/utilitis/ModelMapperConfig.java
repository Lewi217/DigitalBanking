package com.mycompany.app.utilitis;

import com.mycompany.app.dto.UserDto;
import com.mycompany.app.model.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.addMappings(new PropertyMap<User, UserDto>() {
            @Override
            protected void configure() {
                map().setName(source.getFirstName() + " " + source.getLastName());
            }
        });

        return modelMapper;
    }
}
