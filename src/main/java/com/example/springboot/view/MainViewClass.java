package com.example.springboot.view;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MainViewClass{
	
	public static void mainView() {
		//设置为默认外观界面
//		JFrame.setDefaultLookAndFeelDecorated(true);
		//创建以及设计窗口
		JFrame frame = new JFrame("hello world");
		frame.setBounds(400, 400, 600, 400);
		frame.setResizable(false);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setSize(400, 200);
		JLabel label = new JLabel("用户界面!",JLabel.LEFT);
		label.setSize(100, 50);
		label.setForeground(Color.RED);
		JTextArea text = new JTextArea(1,30);
		text.setSize(200, 100);
		JLabel label2 = new JLabel("评论界面!",JLabel.LEFT);
		label2.setSize(100, 50);
		label2.setForeground(Color.RED);
		panel.add(label,BorderLayout.EAST);
		panel.add(text,BorderLayout.SOUTH);
		panel.add(label2,BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(panel);
		frame.getContentPane().add(scrollPane);
		//设置后,界面根据子组件自适应,设置的宽高无效
//		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	
	public static void main(String[] args) {
		mainView();
	}

}
