package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();


    //过滤敏感词的逻辑,返回的是处理掉敏感词的语句
    public  String filter(String text){
        if (StringUtils.isBlank(text)) {
            return null;
        }

        //定义三个指针
        //指针1
        TrieNode tempNode=rootNode;

        //指针2
        int begin=0;
        //指针3
        int position=0;
        //结果
        StringBuilder sb=new StringBuilder();

        while (position<text.length()){
            char c = text.charAt(position);

            //跳过符号,过滤在敏感词中穿插特殊符号地情况
            if(isSymbol(c)){
                //如此时的指针1处于根节点，就将此特殊符号计入结果中，然后指针2走一步
                if(tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                //指针3移动
                position++;
                continue;
            }
            //检查子结点
            tempNode = tempNode.getSubNode(c);

            if(tempNode==null){
                //表示以begin开头子串不是敏感词
                sb.append(text.charAt(begin));
                ++begin;
                position=begin;
                // 重新指向根节点
                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd()){
                //发现以begin开头的position结尾的敏感词
                sb.append(REPLACEMENT);
                ++position;
                begin=position;
                //指针1重新指向头部指针
                tempNode=rootNode;
            }else{
                //表示遇到的是敏感词的中间部分
                position++;
            }
        }
        //将最后的一批没有匹配上的非敏感词加入到结果中
        sb.append(text.substring(begin));
        return sb.toString();


    }
    //判断是否为特殊符号
    public boolean isSymbol(Character c){
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /*初始化前缀树*/
    @PostConstruct
    public void init(){
        try
                (
        InputStream is = this.getClass().
                getClassLoader().getResourceAsStream("sensitive-words.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                )
        {
            String keyword;
            while ((keyword=reader.readLine())!=null){
                //添加到前缀树中
                this.addkeyword(keyword);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败"+e.getMessage());
        }

    }

    //将敏感词添加到前缀树中
    private void addkeyword(String keyword){
        TrieNode tempnode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {

            char c = keyword.charAt(i);
            TrieNode subNode = tempnode.getSubNode(c);
            if (subNode==null) {
                //没有子节点就初始化子结点
                subNode=new TrieNode();
                tempnode.addSubNode(c,subNode);

            }

            /*指向子结点，进入下一轮循环*/
            tempnode=subNode;


            //设置结束标识
            if (i==keyword.length()-1) {
                tempnode.setKeywordEnd(true);
            }




        }
    }



    //构造前缀树结构
    private class TrieNode{




        //关键词结束标识
        private boolean isKeywordEnd=false;

        //子结点（key是下级字符value是下级结点）
        private Map<Character,TrieNode> subNodes=new HashMap<>();
        //初始化

        //构造
        public boolean isKeywordEnd(){
            return  isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd){
            isKeywordEnd=keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }
        //获取子结点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }


    }


}
