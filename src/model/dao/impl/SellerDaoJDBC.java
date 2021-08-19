package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DbException;
import db.DbIntegrityException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {

	private Connection conn;

	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Seller obj) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(
					"INSERT INTO seller "
					+ "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
					+ "VALUES (?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
					ps.setString(1, obj.getName());
					ps.setString(2, obj.getEmail());
					ps.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
					ps.setDouble(4, obj.getBaseSalary());
			 		ps.setInt(5, obj.getDepartment().getId());
			 		
			 		int rowsAffected = ps.executeUpdate();
			 		if (rowsAffected > 0) {
			 			ResultSet rs = ps.getGeneratedKeys(); 
			 			if (rs.next()) {
			 				int id = rs.getInt(1);
			 				obj.setId(id);
			 			}
			 			DB.closeResultSet(rs);
			 		} else {
			 			throw new DbException("Unexpected error! No rows affected!");
			 		}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}
	}

	@Override
	public void update(Seller obj) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(
					"UPDATE seller "
					+ "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
					+ "WHERE Id = ?");
					ps.setString(1, obj.getName());
					ps.setString(2, obj.getEmail());
					ps.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
					ps.setDouble(4, obj.getBaseSalary());
			 		ps.setInt(5, obj.getDepartment().getId());
			 		ps.setInt(6, obj.getId());
			 		ps.executeUpdate();
			 		
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}

	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("DELETE FROM seller WHERE Id = ?");
			ps.setInt(1, id);
			int rows = ps.executeUpdate();
			if (rows == 0 ) {
				throw new DbException("Id doesn't exists in table seller");
			}
		} catch (SQLException e) {
			throw new DbIntegrityException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}
	}

	@Override
	public Seller findbyId(Integer id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT seller.*, department.Name " + "as DepName FROM seller "
					+ "INNER JOIN department " + "ON seller.DepartmentId = departmentId " + "WHERE seller.Id = ?");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				Department dep = instantiateDepartment(rs);
				Seller obj = instantiateSeller(rs, dep);
				return obj;
			}
			return null;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<Seller> findAll() {PreparedStatement ps = null;
	ResultSet rs = null;
	try {
		ps = conn.prepareStatement(
				"SELECT seller.*, department.Name as DepName "
				+ "FROM seller INNER JOIN department "
				+ "ON seller.DepartmentId = department.Id "
				+ "ORDER BY Name");
		rs = ps.executeQuery();
		List<Seller> list = new ArrayList<>();
		Map<Integer, Department> map = new HashMap<>();
		while (rs.next()) {
			Department dep = map.get(rs.getInt("DepartmentId"));
			if (dep == null) {
				dep = instantiateDepartment(rs);
				map.put(rs.getInt("DepartmentId"), dep);
			}
			Seller obj = instantiateSeller(rs, dep);
			list.add(obj);
		}
		return list;
	} catch (SQLException e) {
		throw new DbException(e.getMessage());
	} finally {
		DB.closeStatement(ps);
		DB.closeResultSet(rs);
	}
	}

	@Override
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(
					"SELECT seller.*, department.Name as DepName "
					+ "FROM seller INNER JOIN department "
					+ "ON seller.DepartmentId = department.Id "
					+ "WHERE DepartmentId = ? " 
					+ "ORDER BY Name");
			ps.setInt(1, department.getId());
			rs = ps.executeQuery();
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			while (rs.next()) {
				Department dep = map.get(rs.getInt("DepartmentId"));
				if (dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}
				Seller obj = instantiateSeller(rs, dep);
				list.add(obj);
			}
			return list;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
			DB.closeResultSet(rs);
		}
	}

	private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException {
		Seller obj = new Seller();
		obj.setId(rs.getInt("id"));
		obj.setName(rs.getString("Name"));
		obj.setEmail(rs.getString("Email"));
		obj.setBirthDate(rs.getDate("BirthDate"));
		obj.setBaseSalary(rs.getDouble("BaseSalary"));
		obj.setDepartment(dep);
		return obj;
	}

	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		Department dep = new Department();
		dep.setId(rs.getInt("DepartmentId"));
		dep.setName(rs.getString("DepName"));
		return dep;
	}
}