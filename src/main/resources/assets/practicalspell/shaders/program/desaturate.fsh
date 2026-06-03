#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform vec2 OutSize;

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D NormalsSampler;   // 法线纹理，通常存在

float sobelNormal(vec2 uv) {
    vec2 texel = 1.0 / OutSize;

    vec3 n00 = texture(NormalsSampler, uv + vec2(-texel.x, -texel.y)).rgb;
    vec3 n01 = texture(NormalsSampler, uv + vec2(-texel.x,  0.0)).rgb;
    vec3 n02 = texture(NormalsSampler, uv + vec2(-texel.x,  texel.y)).rgb;

    vec3 n10 = texture(NormalsSampler, uv + vec2(0.0, -texel.y)).rgb;
    vec3 n12 = texture(NormalsSampler, uv + vec2(0.0,  texel.y)).rgb;

    vec3 n20 = texture(NormalsSampler, uv + vec2(texel.x, -texel.y)).rgb;
    vec3 n21 = texture(NormalsSampler, uv + vec2(texel.x,  0.0)).rgb;
    vec3 n22 = texture(NormalsSampler, uv + vec2(texel.x,  texel.y)).rgb;

    // 对法线三个分量分别计算梯度，然后取长度
    vec3 gx = (n20 + 2.0*n21 + n22) - (n00 + 2.0*n01 + n02);
    vec3 gy = (n02 + 2.0*n12 + n22) - (n00 + 2.0*n10 + n20);
    return length(gx) + length(gy);   // 或者 sqrt(dot(gx,gx)+dot(gy,gy))
}

// Sobel 算子检测深度边缘
float sobelDepth(vec2 uv) {
    vec2 texel = 3.0 / OutSize;
    
    float d00 = texture(DepthSampler, uv + vec2(-texel.x, -texel.y)).r;
    float d01 = texture(DepthSampler, uv + vec2(-texel.x,  0.0)).r;
    float d02 = texture(DepthSampler, uv + vec2(-texel.x,  texel.y)).r;
    
    float d10 = texture(DepthSampler, uv + vec2(0.0, -texel.y)).r;
    float d12 = texture(DepthSampler, uv + vec2(0.0,  texel.y)).r;
    
    float d20 = texture(DepthSampler, uv + vec2(texel.x, -texel.y)).r;
    float d21 = texture(DepthSampler, uv + vec2(texel.x,  0.0)).r;
    float d22 = texture(DepthSampler, uv + vec2(texel.x,  texel.y)).r;
    
    float gx = (d20 + 2.0*d21 + d22) - (d00 + 2.0*d01 + d02);
    float gy = (d02 + 2.0*d12 + d22) - (d00 + 2.0*d10 + d20);
    
    return sqrt(gx*gx + gy*gy);
}

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    float depth = texture(DepthSampler, texCoord).r;
    
    // ========== 深度描边 ==========
    float edge = sobelDepth(texCoord);
    
    // 放大边缘响应（原始深度差太小）
    edge *= 200.0;

    // 自适应阈值：近处更敏感，远处更宽松
    float threshold = mix(0.1, 0.5, depth);
    float depthEdge = sobelDepth(texCoord);
    float normalEdge = sobelNormal(texCoord);

    // 只有“深度边缘”和“法线边缘”都强的地方才保留描边
    float edge = depthEdge * normalEdge;   // 乘法比 min 更能抑制弱边缘

    // 后续统一用这个 edge 值，替换原来的
    edge = smoothstep(threshold * 0.5, threshold, edge) * depthFade;
    
    // ========== 你的原有风格 ==========
    float depthFactor = clamp((1.0 - depth) * 300.0, 0.0, 1.0);
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    float inverted = 1.0 - gray;
    float result = inverted * depthFactor;
    
    // 描边颜色：黑色
    vec3 outlineColor = vec3(0.0);
    
    // 混合：描边处覆盖黑色，其余保持反色风格
    vec3 finalColor = mix(vec3(result), outlineColor, edge);
    
    fragColor = vec4(finalColor, color.a);
}