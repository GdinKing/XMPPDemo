package com.android.king.xmppdemo.xmpp;

import org.jivesoftware.smack.packet.ExtensionElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月14日 13:12分
 * @since 2018-09-14
 * @author king
 */
public class FixedQueryXElement implements ExtensionElement {

    private String mSource;

    private static final String REGEX = "(<field var='[\\w]{4,8}' type='boolean'><value>true</value></field>)";

    public FixedQueryXElement(String source) {
        this.mSource = source;
    }

    @Override
    public String getNamespace() {
        return "jabber:x:data";
    }

    @Override
    public String getElementName() {
        return "x";
    }

    @Override
    public CharSequence toXML() {
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(mSource);
        String des = mSource;
        while (matcher.find()) {
            String group = matcher.group();
            des = des.replace(group, group.replace("<value>true</value>", "<value>1</value>"));
        }
        return des;
    }
}
