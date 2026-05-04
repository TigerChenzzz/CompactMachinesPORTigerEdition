package com.yumocmspor.core;

public class RateEvaluator {
    /**
     * @param timeSeriesData 按秒聚合的 IO 数据数组 (例如 length = 300)
     * @return 拟合后的稳定 tick 产率 (k值)
     */
    public static double evaluateStableRate(int[] timeSeriesData) {
        int n = timeSeriesData.length;
        if (n == 0) return 0;

        // 1. 计算累积量 (Cumulative Sum)
        // C[t] 表示前 t 秒的总 IO 量
        long[] C = new long[n + 1];
        for (int i = 0; i < n; i++) {
            C[i + 1] = C[i] + timeSeriesData[i];
        }

        // 2. 设定参考区 (假设最后 1/3 的时间段机器必然已经稳定)
        int tRef = n * 2 / 3;
        if (n - tRef <= 0) return 0; // 防止数组过小的边界情况

        // 计算参考区间的基准每秒产率 (斜率)
        double kRef = (double) (C[n] - C[tRef]) / (n - tRef);

        // 3. 计算参考区内的最大振荡幅度
        double maxDev = 0;
        for (int t = tRef; t <= n; t++) {
            // E_t 是从终点逆向推演的理论累积量
            double expectedC = C[n] - kRef * (n - t);
            double deviation = C[t] - expectedC;
            if (Math.abs(deviation) > maxDev) {
                maxDev = Math.abs(deviation);
            }
        }

        // 4. 设定容忍度阈值
        // 容忍度 = max(振荡幅度的 2 倍, 产率的 1.5 倍, 极小值保护)
        double tolerance = Math.max(maxDev * 2.0, Math.max(kRef * 1.5, 1.0));

        // 5. 逆向寻找预热期/缓存倾泻期的边界
        int stableStart = 0;
        for (int t = tRef - 1; t >= 0; t--) {
            double expectedC = C[n] - kRef * (n - t);
            double deviation = Math.abs(C[t] - expectedC);

            // 如果偏差超过了稳定期的容忍度，说明 t 时刻处于非稳定态(预热或倾泻)
            if (deviation > tolerance) {
                stableStart = t + 1; // 稳定期从下一个时刻开始
                break;
            }
        }

        // 6. 计算最终结果
        int stableDuration = n - stableStart;
        if (stableDuration <= 0) return 0;

        double finalRatePerSecond = (double) (C[n] - C[stableStart]) / stableDuration;
        // 换算为每 tick 的产率 (除以 20)
        return finalRatePerSecond / 20.0;
    }
}
