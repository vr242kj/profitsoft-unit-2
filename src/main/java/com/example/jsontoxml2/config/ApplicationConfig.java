package com.example.jsontoxml2.config;

import com.example.jsontoxml2.model.dto.post.PostCreateRequestDto;
import com.example.jsontoxml2.model.entity.Post;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(PostCreateRequestDto.class, Post.class)
                .addMappings(mapper -> mapper.skip(Post::setId));

        return modelMapper;

    }

}
