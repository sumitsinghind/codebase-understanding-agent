import streamlit as st
from langchain_community.vectorstores import FAISS
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.llms import Ollama

st.title("AI Codebase Understanding Agent")

query = st.text_input("Ask a question about the codebase")

embeddings = HuggingFaceEmbeddings()

db = FAISS.load_local(
    "vector_db",
    embeddings,
    allow_dangerous_deserialization=True
)

llm = Ollama(model="llama3")

if query:
    docs = db.similarity_search(query, k=3)

    context = "\n".join([doc.page_content for doc in docs])

    prompt = f"""
You are an expert software engineer.

Use the following code context to answer the question.

Context:
{context}

Question:
{query}
"""

    response = llm.invoke(prompt)

    st.write(response)