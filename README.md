# canned-renderer
A small 3d software renderer written in Java.

This is just a small 3d software renderer I wrote for fun. It uses java.awt for drawing to the screen.
The only built-in graphics function used were fillRect to set an individual pixel and to clear the framebuffer.
For matrix multiplication, I used jblas, which is similar to numpy but is a library for Java.

Features
-----------------------------------------------------------------------------------

* Phong diffuse lighting
* Texture mapping
* Loading 3d models from .obj files

Limitations
------------------------------------------------------------------------------------
* .obj model loader can only load models with split edges due to handling of texture indices.
* Camera is at a fixed position because I didn't implement a proper model-view matrix.

Penguin model for demonstration purposes was made by nabarun1011:
https://free3d.com/3d-model/emperor-penguin-601811.html
