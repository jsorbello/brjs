package org.bladerunnerjs.utility.trie.node;

public interface TrieNode<T> extends Comparable<TrieNode<T>>
{
	TrieNode<T> getNextNode(char character);
	char getChar();
	TrieNode<T> getOrCreateNextNode(char character);
	T getValue();
	void setValue(T value);
	TrieNode<T>[] getChildren();
}
