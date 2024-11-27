package edu.escuelaing.arsw.puko.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageResponseDTO {

    private Long id;
    private byte[] data;
    private String filename; // Stores the name of the image file

}
