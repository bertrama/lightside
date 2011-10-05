package edu.cmu.side.feature.cluster;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class BackedMap<K, V> implements Map<K, V>
{
    PreparedStatement createStatement;
    PreparedStatement putStatement;
    PreparedStatement getStatement;
    PreparedStatement allStatement;
    PreparedStatement clearStatement;
    PreparedStatement removeStatement;
    Connection connection;
    String mapName;
	private PreparedStatement putAllStatement;

	public BackedMap(String mapName)
	{
		try
		{
			this.mapName = mapName;
			Class.forName("org.sqlite.JDBC");
		    connection =
		      DriverManager.getConnection("jdbc:sqlite:backedMap.db");
		    Statement stat = connection.createStatement();
		    stat.executeUpdate("drop table if exists "+mapName+";");
		    stat.executeUpdate("create table "+mapName+" (keycode int, key primary key, value);");

		    stat.executeUpdate("CREATE INDEX if not exists keyCodeIndex ON "+mapName +" (keycode)");
		    
		     createStatement = connection.prepareStatement("insert into "+mapName+" values (?, ?, ?);");
		     //putAllStatement = connection.prepareStatement("UPDATE "+mapName+" SET value=?, keycode=? WHERE key=?; IF @@ROWCOUNT=0 INSERT INTO "+mapName+" VALUES (?, ?, ?)");
		     putStatement = connection.prepareStatement("insert or replace into "+mapName+" values(?, ?, ?)");
		     putAllStatement = connection.prepareStatement("insert or replace into "+mapName+" values(?, ?, ?)");
		     getStatement = connection.prepareStatement("select * from "+mapName+" where keycode = ?;");
		     allStatement = connection.prepareStatement("select * from "+mapName);
		     clearStatement = connection.prepareStatement("delete from "+mapName+";");
		     removeStatement = connection.prepareStatement("delete from "+mapName+" where keycode = ?;");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	
	public boolean containsKey(Object key)
	{		
		try
		{
			return get(key) != null;
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public Set<K> keySet()
	{
		Set<K> keys = new HashSet<K>();
		try
		{
			ResultSet results = allStatement.executeQuery();
			while(results.next())
			{
				keys.add((K)results.getObject("key"));
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keys;
	}
	
	public V put(K key, V value)
	{
		
		try
		{
			//getStatement.setInt(1, key.hashCode());
			//ResultSet result = getStatement.executeQuery();
			//V old = null;
			//if(result.next())
			{
//				K oldK = (K) result.getObject("key");
//				old = (V) result.getObject("value");
				
//				if(! oldK.equals(key))
//				{
//					System.err.println("****(BackedMap) Keys Disagree: "+oldK+" vs "+key);
//				}

				 putStatement.clearParameters();
			     putStatement.setInt(1, key.hashCode());
			     putStatement.setObject(2, key);
			     putStatement.setObject(3, value);
			     putStatement.execute();
			}
//			else
//			{
//			     createStatement.setInt(1, key.hashCode());
//			     createStatement.setObject(2, key);
//			     createStatement.setObject(3, value);
//			     createStatement.execute();
//			}
			return null;
			
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public V get(Object key)
	{
		try
		{
			getStatement.setInt(1, key.hashCode());
			ResultSet result = getStatement.executeQuery();
			//if(!result.next()) return null;
			//else 
			while(result.next())
			{
				K inKey = (K)result.getObject("key");
				if(key.equals(inKey) || inKey.equals(key.toString()))
					return (V) result.getObject("value");
			}
			return null;
			
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException
	{
		BackedMap<String, Double> map = new BackedMap<String, Double>("test");

		HashMap<String, Double> fish = new HashMap<String, Double>();
		
		fish.put("halibut", -7.0);
		fish.put("trout", 99.0);
		
		map.putAll(fish);
		
		fish.put("trout", 0.0);
		map.putAll(fish);
		
		System.out.println("bar?"+map.get("bar"));
		System.out.println(map.containsKey("bar"));
		System.out.println("foo? "+map.get("foo"));
		System.out.println(map.containsKey("foo"));
		map.put("foo", new Double(5));
		System.out.println("foo? "+map.get("foo"));
		System.out.println(map.containsKey("foo"));
		map.put("foo", new Double(6));
		System.out.println("foo? "+map.get("foo"));
		System.out.println(map.containsKey("foo"));
		map.put("bar", new Double(7));
		System.out.println("bar?"+map.get("bar"));
		System.out.println(map.containsKey("bar"));
		System.out.println("foo? "+map.get("foo"));
		System.out.println(map.containsKey("foo"));
		
		for(String k : map.keySet())
		{
			System.out.println(k+": "+map.get(k));
		}
	}


	@Override
	public void clear()
	{
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean containsValue(Object arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isEmpty()
	{
		try
		{
			return !allStatement.executeQuery().next();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
	}


	@Override
	public void putAll(Map<? extends K, ? extends V> map)
	{
		try
		{

		    putAllStatement = connection.prepareStatement("insert into "+mapName+" values(?, ?, ?)");
			for(K k : map.keySet())
			{
				//put(k, map.get(k));
			
				
				putAllStatement.setInt(1, k.hashCode());
				putAllStatement.setObject(2, k);
				putAllStatement.setObject(3, map.get(k));
				putAllStatement.addBatch();
			}

			// turn off autocommit
			connection.setAutoCommit(false);
			// submit the batch for execution
			putAllStatement.executeBatch();
			connection.setAutoCommit(true);
		  

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}


	@Override
	public V remove(Object key)
	{
		V old = get(key);
		try
		{
			if(old != null)
			{
			     removeStatement.setObject(1, key);
			     removeStatement.execute();
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return old;
	}


	@Override
	public int size()
	{
		// TODO Auto-generated method stub
		return keySet().size();
	}


	@Override
	public Collection<V> values()
	{
		Collection<V> values = new ArrayList<V>();
		try
		{
			ResultSet results = allStatement.executeQuery();
			while(results.next())
			{
				values.add((V)results.getObject("value"));
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return values;
	}
}
