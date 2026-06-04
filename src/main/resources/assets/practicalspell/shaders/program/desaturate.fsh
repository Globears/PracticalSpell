#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform vec2 OutSize;

in vec2 texCoord;
out vec4 fragColor;


void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    float depth = texture(DepthSampler, texCoord).r;
    
    // ========== 你的原有风格 ==========
    float depthFactor = clamp((1.0 - depth) * 500.0, 0.0, 1.0);
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    float inverted = 1.0 - gray;
    float result = inverted * depthFactor;


    
    fragColor = vec4(finalColor, color.a);
}