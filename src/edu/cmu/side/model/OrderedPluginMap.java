package edu.cmu.side.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import edu.cmu.side.plugin.SIDEPlugin;

public class OrderedPluginMap implements SortedMap<SIDEPlugin, Map<String, String>>, Serializable
{

	private List<SIDEPlugin> ordering = new ArrayList<SIDEPlugin>();
	private Map<SIDEPlugin, Map<String, String>> configurations = new HashMap<SIDEPlugin, Map<String, String>>();

	public int getOrdering(Object s)
	{
		return ordering.indexOf(s);
	}
	

	private transient OrderedPluginComparator comparator = new OrderedPluginComparator(this);

	@Override
	public void clear()
	{
		ordering.clear();
		configurations.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return configurations.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return configurations.containsValue(value);
	}

	@Override
	public Map<String, String> get(Object key)
	{
		return configurations.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return configurations.isEmpty();
	}

	@Override
	public Map<String, String> put(SIDEPlugin key, Map<String, String> value)
	{
		ordering.add(key);
		configurations.put(key, value);
		return value;
	}

	@Override
	public void putAll(Map<? extends SIDEPlugin, ? extends Map<String, String>> m)
	{
		for (SIDEPlugin k : m.keySet())
		{
			ordering.add(k);
			configurations.put(k, m.get(k));
		}
	}

	@Override
	public Map<String, String> remove(Object key)
	{
		Map<String, String> value = configurations.get(key);
		ordering.remove(key);
		configurations.remove(key);
		return value;
	}

	@Override
	public int size()
	{
		return configurations.size();
	}

	@Override
	public Comparator<? super SIDEPlugin> comparator()
	{
		return comparator;
	}

	@Override
	public Set<Map.Entry<SIDEPlugin, Map<String, String>>> entrySet()
	{
		return configurations.entrySet();
	}

	@Override
	public SIDEPlugin firstKey()
	{
		return ordering.get(0);
	}

	@Override
	public SortedMap<SIDEPlugin, Map<String, String>> headMap(SIDEPlugin arg0)
	{
		OrderedPluginMap sub = new OrderedPluginMap();
		for (int i = 0; i < ordering.size(); i++)
		{
			if (ordering.get(i).equals(arg0))
			{
				return sub;
			}
			else
			{
				sub.put(ordering.get(i), configurations.get(ordering.get(i)));
			}
		}
		return sub;
	}

	@Override
	public Set<SIDEPlugin> keySet()
	{
		return configurations.keySet();
	}

	@Override
	public SIDEPlugin lastKey()
	{
		return ordering.get(ordering.size() - 1);
	}

	@Override
	public SortedMap<SIDEPlugin, Map<String, String>> subMap(SIDEPlugin first, SIDEPlugin last)
	{
		OrderedPluginMap sub = new OrderedPluginMap();
		boolean start = false;
		for (int i = 0; i < ordering.size(); i++)
		{
			if (!start && first.equals(ordering.get(i)))
			{
				start = true;
			}
			if (start)
			{
				if (ordering.get(i).equals(last))
				{
					return sub;
				}
				else
				{
					sub.put(ordering.get(i), configurations.get(ordering.get(i)));
				}
			}
		}
		return sub;
	}

	@Override
	public SortedMap<SIDEPlugin, Map<String, String>> tailMap(SIDEPlugin arg0)
	{
		OrderedPluginMap sub = new OrderedPluginMap();
		for (int i = ordering.size() - 1; i >= 0; i--)
		{
			sub.put(ordering.get(i), configurations.get(ordering.get(i)));
			if (ordering.get(i).equals(arg0)) { return sub; }
		}
		return sub;
	}

	@Override
	public Collection<Map<String, String>> values()
	{
		return configurations.values();
	}

	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{

		ordering = new ArrayList<SIDEPlugin>();
		configurations = new HashMap<SIDEPlugin, Map<String, String>>();

		List<Serializable> orderedPlugins = (List<Serializable>) in.readObject();
		Map<Serializable, Map<String, String>> pluginConfigurations = (Map<Serializable, Map<String, String>>) in.readObject();
		
		for(Serializable pug : orderedPlugins)
		{
			SIDEPlugin plugin = SIDEPlugin.fromSerializable(pug);
			ordering.add(plugin);
			
			configurations.put(plugin, pluginConfigurations.get(pug));
		}
		
		comparator = new OrderedPluginComparator(this);
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		List<Serializable> orderedPlugins = new ArrayList<Serializable>();
		Map<Serializable, Map<String, String>> pluginConfigurations = new HashMap<Serializable, Map<String,String>>();
		
		for(SIDEPlugin plugin : ordering)
		{
			Serializable pug = plugin.toSerializable();
			orderedPlugins.add(pug);
			pluginConfigurations.put(pug, configurations.get(plugin));
		}
		
		out.writeObject(orderedPlugins);
		out.writeObject(pluginConfigurations);
		
	}

}

class OrderedPluginComparator<SIDEPlugin> implements Comparator<SIDEPlugin>, Serializable
{

	private OrderedPluginMap map;

	public OrderedPluginComparator(OrderedPluginMap m)
	{
		map = m;
	}

	@Override
	public int compare(SIDEPlugin arg0, SIDEPlugin arg1)
	{
		return map.getOrdering(arg0) - map.getOrdering(arg1);
	}

}
