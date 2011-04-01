/*
 *   Wormhole X-Treme Plugin for Bukkit
 *   Copyright (C) 2011  Ben Echols
 *                       Dean Bailey
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wormhole_xtreme.wormhole.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;

import com.wormhole_xtreme.wormhole.WormholeXTreme;



// TODO: Auto-generated Javadoc
/**
 * The Class StargateShape.
 */
public class StargateShape 
{
	
	/** The shape name. */
	public String shapeName = "Standard";
	
	/** The stargate_positions. */
	public int[][] stargate_positions = { {0,2,0}, {0,3,0}, {0,4,0}, 
		{0,1,1}, {0,5,1}, 
		{0,0,2}, {0,6,2}, 
		{0,6,3}, {0,0,3}, 
		{0,0,4}, {0,6,4}, 
		{0,5,5}, {0,1,5}, 
		{0,2,6}, {0,3,6}, {0,4,6} };
	
	/** The sign_position. */
	public int[] sign_position = {0,3,6};
	
	/** The enter_position. */
	public int[] enter_position = {0,0,3};
	
	/** The light_positions. */
	public int[] light_positions = {3,4,11,12};
	
	/** The water_positions. */
	public int[][] water_positions = { {0,2,1}, {0,3,1}, {0,4,1}, 
			{0,1,2}, {0,2,2}, {0,3,2}, {0,4,2}, {0,5,2}, 
			{0,1,3}, {0,2,3}, {0,3,3}, {0,4,3}, {0,5,3}, 
			{0,1,4}, {0,2,4}, {0,3,4}, {0,4,4}, {0,5,4}, 
			{0,2,5}, {0,3,5}, {0,4,5} };
	
	/** The reference_vector. */
	public int[] reference_vector = {0,1,0};
	
	/** [0] = Left - / Right + [1] = Up + / Down - [2] = Forward + / Backward -. */
	public int[] to_gate_corner = {1,-1, 4};
	
	/** The woosh_depth. */
	public final int woosh_depth;
	/** The square of the woosh_depth, used in comparisions with squared distance */
	public final int woosh_depth_squared;
	
	public Material portal_material = Material.STATIONARY_WATER;
	public Material iris_material = Material.STONE;
	public Material stargate_material = Material.OBSIDIAN;
	public Material active_material = Material.GLOWSTONE;
	
	/**
	 * Instantiates a new stargate shape.
	 */
	public StargateShape()
	{
		woosh_depth = 3;
		woosh_depth_squared = 9;
	}
	
	/**
	 * Instantiates a new stargate shape.
	 *
	 * @param file_data the file_data
	 */
	public StargateShape(String[] file_data)
	{
		sign_position = null;
		enter_position = null;
		
		ArrayList<Integer[]> block_positions = new ArrayList<Integer[]>();
		ArrayList<Integer[]> portal_positions = new ArrayList<Integer[]>();
		ArrayList<Integer> light_positions = new ArrayList<Integer>();
		
		int num_blocks = 0;
		int cur_woosh_depth = 3;
		
		// 1. scan all lines for lines beginning with [  - that is the height of the gate
		int height = 0;
		int width = 0;
		for ( int i = 0; i < file_data.length; i++ )
		{
			String line = file_data[i];
			
			if ( line.contains("Name=") )
			{
				this.shapeName = line.split("=")[1];
				WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Begin parsing shape: \"" + (String)this.shapeName + "\"");
			}
			else if ( line.equals("GateShape=") )
			{
				int index = i + 1;
				while ( file_data[index].startsWith("[") )
				{
					if ( width <= 0 )
					{
						Pattern p = Pattern.compile("(\\[.*?\\])");
						Matcher m = p.matcher(file_data[index]);
						while ( m.find() )
							width++;
					}
						
					height++; index++;
				}
					
				// At this point we should know the height and width
				if ( height <= 0 || width <= 0)
				{
				    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to parse custom gate due to incorrect height or width: \"" + (String)this.shapeName + "\"");
					throw new IllegalArgumentException("Unable to parse custom gate due to incorrect height or width: \"" + (String)this.shapeName + "\"");
				}
				else 
				{
				    WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG,false,"Shape: \"" + (String)this.shapeName + "\"" + " Height: \"" + Integer.toString((int)height) + "\"" + " Width: \"" + Integer.toString((int)width) + "\"" );
				}
					
				// Now parse each [X] and put into int array.
				index = i + 1;
				while ( file_data[index].startsWith("[") )
				{
						
					Pattern p = Pattern.compile("(\\[.*?\\])");
					Matcher m = p.matcher(file_data[index]);
					int j = 0;
					while ( m.find() )
					{
						String block = m.group(0);
						Integer[] point = { 0, (height - 1 - (index-i-1)), (width - 1 - j) };
						if ( block.contains("O") )
						{
							num_blocks++;
							block_positions.add(point);
						}
						else if ( block.contains("P") )
						{
							portal_positions.add(point);
						}
						
							
						if ( block.contains("S") || block.contains("E") )
						{
							int[] point_i = new int[3];
							for (int k = 0; k < 3; k++ )
								point_i[k] = point[k];
							
							if ( block.contains("S") )
							{
								sign_position = point_i;
							}
							if ( block.contains("E") )
							{
								enter_position = point_i;
							}
						}
							
						if ( block.contains("L") && block.contains("O") )
						{
							light_positions.add( num_blocks - 1);
						}
							
						j++;
					}
					index++;
				}
			}
			else if ( line.contains("BUTTON_UP") )
			{
				to_gate_corner[1] = Integer.parseInt(line.split("=")[1]);
			}
			else if ( line.contains("BUTTON_RIGHT") )
			{
				to_gate_corner[0] = Integer.parseInt(line.split("=")[1]);
			}
			else if ( line.contains("BUTTON_AWAY") )
			{
				to_gate_corner[2] = Integer.parseInt(line.split("=")[1]);
			}
			else if ( line.contains("WOOSH_DEPTH") )
			{
				cur_woosh_depth = Integer.parseInt(line.split("=")[1]);
			}
			else if ( line.contains("PORTAL_MATERIAL") )
			{
				portal_material = Material.valueOf(line.split("=")[1]);
			}
			else if ( line.contains("IRIS_MATERIAL") )
			{
				iris_material = Material.valueOf(line.split("=")[1]);
			}
			else if ( line.contains("STARGATE_MATERIAL") )
			{
				stargate_material = Material.valueOf(line.split("=")[1]);
			}
			else if ( line.contains("ACTIVE_MATERIAL") )
			{
				active_material = Material.valueOf(line.split("=")[1]);
			}
		}
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Sign Position: \"" + Arrays.toString(sign_position) + "\"");
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Enter Position: \"" + Arrays.toString(enter_position) + "\"");
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Button Position [Left/Right,Up/Down,Forward/Back]: \"" + Arrays.toString((int[])to_gate_corner) + "\"");
		this.water_positions = new int[portal_positions.size()][3];
		for ( int i = 0; i < portal_positions.size(); i++)
		{
			int[] point = new int[3];
			for (int j = 0; j < 3; j++ )
				point[j] = portal_positions.get(i)[j];
			this.water_positions[i] = point;
		}
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Portal Positions: \"" + Arrays.deepToString((int[][])this.water_positions) + "\"");
		
		this.light_positions = new int[light_positions.size()];
		for ( int i = 0; i < light_positions.size(); i++)
		{
			this.light_positions[i] = light_positions.get(i);
		}
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Light Material Positions: \"" + light_positions + "\"");
			
		this.stargate_positions = new int[block_positions.size()][3];
		for ( int i = 0; i < block_positions.size(); i++)
		{
			int[] point = new int[3];
			for (int j = 0; j < 3; j++ )
				point[j] = block_positions.get(i)[j];
			this.stargate_positions[i] = point;
		}
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Material Positions: \"" + Arrays.deepToString((int[][])this.stargate_positions) + "\"");
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Finished parsing shape: \"" + (String)this.shapeName + "\"");

		woosh_depth = cur_woosh_depth;
		woosh_depth_squared = cur_woosh_depth * cur_woosh_depth;
	}
}
