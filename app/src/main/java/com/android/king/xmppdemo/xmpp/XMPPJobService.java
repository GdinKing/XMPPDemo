package com.android.king.xmppdemo.xmpp;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.android.king.xmppdemo.util.Logger;


/***
 *
 * XMPPService保活任务调度
 * @since 2018-09-13
 * @author king
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class XMPPJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            //判断保活的service是否被杀死
            if (!isServiceRunning(XMPPService.class)) {
                //重启service
                startService(new Intent(getApplicationContext(), XMPPService.class));
            }
            reSchedule();
            jobFinished(params, false);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * 重新创建新的调度任务
     */
    private void reSchedule() {
        if (getApplicationContext() == null) {
            return;
        }
        JobScheduler mJobScheduler = (JobScheduler) getApplicationContext()
                .getSystemService(JOB_SCHEDULER_SERVICE);
        //jobId可根据实际情况设定
        JobInfo.Builder mJobBuilder =
                new JobInfo.Builder(0,
                        new ComponentName(getPackageName(),
                                XMPPJobService.class.getName()));

        //30秒后重新执行调度任务
        mJobBuilder.setMinimumLatency(30 * 1000).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

        if (mJobScheduler != null && mJobScheduler.schedule(mJobBuilder.build())
                <= JobScheduler.RESULT_FAILURE) {
            //Scheduled Failed/LOG or run fail safe measures
            Logger.e("执行出错");
        }
    }

    /**
     * 判断服务是否在运行
     *
     * @param serviceClass
     * @return
     */
    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
