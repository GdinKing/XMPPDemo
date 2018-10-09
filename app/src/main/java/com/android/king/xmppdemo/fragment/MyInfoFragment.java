package com.android.king.xmppdemo.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.king.albumpicker.AlbumPicker;
import com.android.king.albumpicker.util.AlbumConstant;
import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.event.UpdateInfoEvent;
import com.android.king.xmppdemo.listener.OnExecuteCallback;
import com.android.king.xmppdemo.net.AsyncExecutor;
import com.android.king.xmppdemo.util.GlideUtil;
import com.android.king.xmppdemo.util.ImageUtil;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigItems;
import com.mylhyl.circledialog.callback.ConfigTitle;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.ItemsParams;
import com.mylhyl.circledialog.params.TitleParams;
import com.mylhyl.circledialog.res.values.CircleDimen;
import com.mylhyl.circledialog.view.listener.OnInputClickListener;
import com.mylhyl.circledialog.view.listener.OnInputCounterChangeListener;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;


/**
 * 个人信息
 */
public class MyInfoFragment extends BaseFragment implements View.OnClickListener {

    public static MyInfoFragment newInstance(User user) {
        MyInfoFragment fragment = new MyInfoFragment();
        Bundle b = new Bundle();
        b.putSerializable("user", user);
        fragment.setArguments(b);
        return fragment;
    }

    private TextView tvAccount;
    private TextView tvNick;
    private TextView tvSign;
    private TextView tvSex;
    private ImageView ivAvatar;

    private User user;

    private View btnAvatar;
    private View btnNick;
    private View btnSign;
    private View btnSex;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        user = (User) args.getSerializable("user");

    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_my_info;
    }

    @Override
    protected void initView() {
        setTitle("个人信息");
        tvAccount = rootView.findViewById(R.id.tv_account);
        tvNick = rootView.findViewById(R.id.tv_nick);
        tvSign = rootView.findViewById(R.id.tv_sign);
        tvSex = rootView.findViewById(R.id.tv_sex);
        ivAvatar = rootView.findViewById(R.id.iv_avatar);
        btnAvatar = rootView.findViewById(R.id.rl_avatar);
        btnNick = rootView.findViewById(R.id.rl_nick);
        btnSex = rootView.findViewById(R.id.rl_sex);
        btnSign = rootView.findViewById(R.id.rl_sign);

        btnAvatar.setOnClickListener(this);
        btnNick.setOnClickListener(this);
        btnSex.setOnClickListener(this);
        btnSign.setOnClickListener(this);


    }

    @Override
    protected void initData() {

        AsyncExecutor.getInstance().execute(new OnExecuteCallback<User>() {
            @Override
            public User onExecute() throws Exception {
                return XMPPHelper.getInstance().getUserInfo(user.getAccount());
            }

            @Override
            public void onFinish(User userInfo, Exception e) {
                if (e != null) {
                    Logger.e(e);
                    showToast("获取用户信息失败");
                    return;
                }
                if (userInfo != null) {
                    Logger.i("获取个人信息" + userInfo.toString());
                    String userAccount = userInfo.getAccount().split("@")[0];
                    String nick = userInfo.getNickName();
                    String userNote = userInfo.getNote();
                    String avatar = userInfo.getAvatar();
                    int sex = userInfo.getSex();

                    tvAccount.setText(userAccount);
                    String sign = userInfo.getSign();
                    if (!TextUtils.isEmpty(sign)) {
                        tvSign.setText(userNote);
                    } else {
                        tvSign.setText("未填写");
                    }
                    if (sex == 0) {
                        tvSex.setText("男");
                    } else if (sex == 1) {
                        tvSex.setText("女");
                    } else {
                        tvSex.setText("未填写");
                    }
                    tvNick.setText(!TextUtils.isEmpty(nick) ? nick : userAccount.split("@")[0]);

                    if (avatar != null) {
                        ImageUtil.showImage(getActivity(), ivAvatar, user.getAvatar());
                    }
                } else {
                    tvAccount.setText(user.getAccount());
                    tvNick.setText(user.getAccount().split("@")[0]);
                    tvSign.setText("未填写");
                    tvSex.setText("未填写");
                    ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                }

            }
        });
    }

    private void checkCameraPermission() {
        //权限申请
        HiPermission.create(getActivity())
                .checkSinglePermission(Manifest.permission.CAMERA, new PermissionCallback() {
                    @Override
                    public void onClose() {
                    }

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onDeny(String permission, int position) {

                    }

                    @Override
                    public void onGuarantee(String permission, int position) {

                    }
                });
    }

    @Override
    public boolean onBackPressedSupport() {
        pop();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_avatar:
                showAvatarSelect();
                break;
            case R.id.rl_nick:
                showNickEdit();
                break;
            case R.id.rl_sex:
                showSexSelect();
                break;
            case R.id.rl_sign:
                showSignEdit();
                break;

        }
    }

    private void showAvatarSelect() {

        new CircleDialog.Builder(getActivity())
                .setTitle("上传头像")
                .setRadius(15)
                .setTitleColor(ContextCompat.getColor(getActivity(), R.color.blue))
                .configTitle(new ConfigTitle() {
                    @Override
                    public void onConfig(TitleParams params) {
                        params.textSize = CircleDimen.SUBTITLE_TEXT_SIZE;
                    }
                })
                //添加标题，参考普通对话框
                .setItems(new String[]{"拍照上传", "从相册中选取"}, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            checkCameraPermission();
                        } else if (position == 1) {
                            Intent intent = AlbumPicker.getInstance(getActivity())
                                    .setImageLoader(new GlideUtil())  //设置图片加载器
                                    .setSelectType(AlbumConstant.TYPE_IMAGE) //选择类型 TYPE_ALL:图片和视频 TYPE_IMAGE:图片 TYPE_VIDEO:视频
                                    .setMode(AlbumConstant.MODE_SINGLE) //选择模式 MODE_MULTI：多选 MODE_SINGLE：单选
                                    .setWidthLimit(800)  //压缩宽度限定，为原图时此设置无效，默认720
                                    .setHeightLimit(800) //压缩高度限定，为原图时此设置无效，默认1280
                                    .getIntent();
                            startActivityForResult(intent, 100);
                        }
                    }
                }).configItems(new ConfigItems() {
            @Override
            public void onConfig(ItemsParams params) {
                params.textColor = Color.BLACK;
            }
        })
                .setNegative("取消", null)
                .configNegative(new ConfigButton() {
                    @Override
                    public void onConfig(ButtonParams params) {
                        params.textSize = CircleDimen.CONTENT_TEXT_SIZE;
                        params.textColor = Color.RED;
                    }
                })
                .show();
    }

    private void showSexSelect() {

        new CircleDialog.Builder(getActivity())
                .setTitle("选择性别")
                .setRadius(15)
                .setTitleColor(ContextCompat.getColor(getActivity(), R.color.blue))
                .configTitle(new ConfigTitle() {
                    @Override
                    public void onConfig(TitleParams params) {
                        params.textSize = CircleDimen.SUBTITLE_TEXT_SIZE;
                    }
                })
                //添加标题，参考普通对话框
                .setItems(new String[]{"男", "女"}, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            user.setSex(0);
                            tvSex.setText("男");
                        } else if (position == 1) {
                            user.setSex(1);
                            tvSex.setText("女");
                        }
                        updateInfo(user, 2);
                    }
                })
                .configItems(new ConfigItems() {
                    @Override
                    public void onConfig(ItemsParams params) {
                        params.textColor = Color.BLACK;
                    }
                })
                .setNegative("取消", null)
                .configNegative(new ConfigButton() {
                    @Override
                    public void onConfig(ButtonParams params) {
                        params.textSize = CircleDimen.CONTENT_TEXT_SIZE;
                        params.textColor = Color.RED;
                    }
                })
                .show();
    }

    private void showNickEdit() {
        String nick = tvNick.getText().toString();
        new CircleDialog.Builder(getActivity())
                //添加标题，参考普通对话框
                .setInputText(nick, "请输入昵称")//输入框默认文本，提示
                .setTitle("修改昵称")
                .configTitle(new ConfigTitle() {
                    @Override
                    public void onConfig(TitleParams params) {
                        params.textSize = CircleDimen.SUBTITLE_TEXT_SIZE;
                    }
                })
                .setTitleColor(ContextCompat.getColor(getActivity(), R.color.blue))
                .setCanceledOnTouchOutside(false)
                .setInputHeight(100)
                .setRadius(15)
                .setInputCounter(12)//输入框的最大字符数，默认格式在输入右下角例如：20
                .autoInputShowKeyboard()//自动弹出键盘
                .setPositiveInput("确定", new OnInputClickListener() {
                    @Override
                    public void onClick(String text, View v) {
                        if (TextUtils.isEmpty(text)) {
                            showToast("昵称不能为空");
                            return;
                        }
                        tvNick.setText(text);
                        user.setNickName(text);
                        updateInfo(user, 1);
                    }
                })
                .setNegative("取消", null)
                .show();
    }

    private void showSignEdit() {
        String sign = tvSign.getText().toString();
        new CircleDialog.Builder(getActivity())
                .setInputText(sign, "请输入个性签名")
                .setTitle("修改个性签名")
                .configTitle(new ConfigTitle() {
                    @Override
                    public void onConfig(TitleParams params) {
                        params.textSize = CircleDimen.SUBTITLE_TEXT_SIZE;
                    }
                })
                .setTitleColor(ContextCompat.getColor(getActivity(), R.color.blue))
                .setCanceledOnTouchOutside(false)
                .setRadius(15)
                .setInputCounter(50, new OnInputCounterChangeListener() {
                    @Override
                    public String onCounterChange(int maxLen, int currentLen) {
                        return currentLen + "/" + maxLen;
                    }
                })
                .autoInputShowKeyboard()//自动弹出键盘
                .setPositiveInput("确定", new OnInputClickListener() {
                    @Override
                    public void onClick(String text, View v) {
                        if (TextUtils.isEmpty(text)) {
                            return;
                        }
                        tvSign.setText(text);
                        user.setSign(text);
                        updateInfo(user, 0);
                    }
                })
                .setNegative("取消", null)
                .show();
    }


    /**
     * 上传头像
     *
     * @param filePath
     */
    private void uploadAvatar(final String filePath) {
        showProgress("上传中");
        AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {
                File f = new File(filePath);
                if (!f.exists()) {
                    throw new RuntimeException("图片不存在");
                }
                Logger.i(filePath);
                XMPPHelper.getInstance().updateAvatar(f);
                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {
                dismissProgress();
                if (e != null) {
                    Logger.e(e);
                    showToast("上传头像失败");
                    return;
                }
                showToast("上传成功");
                user.setAvatar(filePath);
                EventBus.getDefault().post(new UpdateInfoEvent(user));
            }
        });

    }


    /**
     * 修改用户信息
     */
    private void updateInfo(final User userInfo, final int type) {
        AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {

                XMPPHelper.getInstance().saveUserInfo(userInfo);
                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {
                dismissProgress();
                if (e != null) {
                    if (type == 0) {
                        showToast("修改个性签名失败");
                    } else if (type == 1) {
                        showToast("修改昵称失败");
                    } else if (type == 2) {
                        showToast("修改性别失败");
                    }
                    return;
                }
                showToast("修改成功");
                EventBus.getDefault().post(new UpdateInfoEvent(user));
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 100 && data != null) {
            ArrayList<String> pathList = data.getStringArrayListExtra(AlbumConstant.RESULT_KEY_PATH_LIST);
            if (pathList.size() == 1) {
                String path = pathList.get(0);
                ImageUtil.showImage(getActivity(), ivAvatar, path);
                uploadAvatar(path);
            }
        }
    }
}
