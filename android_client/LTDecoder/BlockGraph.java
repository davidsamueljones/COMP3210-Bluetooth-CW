import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

class BlockGraph {
  public final int num_blocks;
  public final Map<Integer, Set<CheckNode>> checks;
  public final Map<Integer, byte[]> eliminated;


  public BlockGraph(int num_blocks) {
    // Initialise the checks map to act like a Python defaultdictionary 
    // (generate a new entry if it on fetch)
    checks = new HashMap<Integer, Set<CheckNode>>() {
      private static final long serialVersionUID = -6967943100546020801L;

      @Override
      public Set<CheckNode> get(Object key) {
        if (!super.containsKey(key)) {
          if (key instanceof Integer) {
            super.put((Integer) key, new HashSet<>());
          } else {
            throw new IllegalArgumentException("Entry cannot be created for non-integer key");
          }
        }
        return super.get(key);
      }
    };
    this.num_blocks = num_blocks;
    this.eliminated = new HashMap<>();
  }

  public boolean add_block(Set<Integer> nodes, byte[] data) {
    // We can eliminate this source node
    if (nodes.size() == 1) {
      Integer node = nodes.iterator().next();
      Set<Entry<Integer, byte[]>> to_eliminate = eliminate(node, data);

      // Recursively eliminate all nodes that can now be resolved
      Iterator<Entry<Integer, byte[]>> itr_to_eliminate = to_eliminate.iterator();
      while (itr_to_eliminate.hasNext()) {
        Entry<Integer, byte[]> next = itr_to_eliminate.next();
        itr_to_eliminate.remove();
        to_eliminate.addAll(eliminate(next.getKey(), next.getValue()));
        // Refresh iterator for new items
        itr_to_eliminate = to_eliminate.iterator();
      }
    } else {
      // Pass messages from already-resolved source nodes
      Iterator<Integer> itr_nodes = nodes.iterator();
      while (itr_nodes.hasNext()) {
        int node = itr_nodes.next();
        if (eliminated.containsKey(node)) {
          itr_nodes.remove();
          data = xor_by_byte(data, eliminated.get(node), data.length);
        }
      }
      // Resolve if we are left with a single non-resolved source node
      if (nodes.size() == 1) {
        return add_block(nodes, data);
      } else {

        // Add edges for all remaining nodes to this check
        CheckNode check = new CheckNode(nodes, data);
        for (Integer node : nodes) {
          checks.get(node).add(check);
        }
      }
    }
    return eliminated.size() >= num_blocks;
  }

  public Set<Entry<Integer, byte[]>> eliminate(Integer node, byte[] data) {
    Set<Entry<Integer, byte[]>> resolved = new HashSet<>();

    // Cache resolved value
    eliminated.put(node, data.clone());
    Set<CheckNode> others = checks.get(node);
    checks.remove(node);

    // Pass messages to all associated checks
    for (CheckNode check : others) {
      check.check = xor_by_byte(check.check, data, data.length);
      check.src_nodes.remove(node);

      // Return all nodes that can now be resolved
      if (check.src_nodes.size() == 1) {
        resolved.add(new AbstractMap.SimpleEntry<>(check.src_nodes.iterator().next(), check.check));
      }
    }
    return resolved;
  }

  protected byte[] xor_by_byte(byte[] a, byte[] b, int length) {
    byte[] xored = new byte[length];
    for (int i = 0; i < length; i++) {
      xored[i] = (byte) (a[i] ^ b[i]);
    }
    return xored;
  }

  
  public Iterator<byte[]> get_block_bytes_iterator() {
    List<Entry<Integer, byte[]>> list = new ArrayList<>(eliminated.entrySet());
    list.sort(new Comparator<Entry<Integer, byte[]>>() {
      @Override
      public int compare(Map.Entry<Integer, byte[]> o1, Map.Entry<Integer, byte[]> o2) {
        return Integer.compare(o1.getKey(), o2.getKey());
      }
    });
    // Extract byte arrays from node entry
    List<byte[]> block_bytes = new ArrayList<>();
    for (Entry<Integer, byte[]> entry : list) {
      block_bytes.add(entry.getValue());
    }
    return block_bytes.iterator();
  }

  class CheckNode {
    public final Set<Integer> src_nodes;
    public byte[] check;

    public CheckNode(Set<Integer> src_nodes, byte[] check) {
      this.src_nodes = src_nodes;
      this.check = check.clone();
    }
  }
}


